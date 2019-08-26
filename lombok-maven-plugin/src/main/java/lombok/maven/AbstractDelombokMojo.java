package lombok.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import lombok.launch.Delombok;

/**
 * Abstract mojo to Delombok java source with lombok annotations.
 *
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
public abstract class AbstractDelombokMojo extends AbstractMojo {

    /**
     * Specifies whether the delombok generation should be skipped.
     */
    @Parameter(property="lombok.delombok.skip", defaultValue="false", required=true)
    protected boolean skip;

    /**
     * Encoding.
     */
    @Parameter(property="lombok.encoding", defaultValue="${project.build.sourceEncoding}", required=true)
    protected String encoding;

    /**
     * Verbose flag.  Print the name of each file as it is being delombok-ed.
     */
    @Parameter(property="lombok.verbose", defaultValue="false", required=true)
    protected boolean verbose;

    /**
     * Add output directory flag.  Adds the output directory to the Maven build path.
     */
    @Parameter(property="lombok.addOutputDirectory", defaultValue="true", required=true)
    protected boolean addOutputDirectory;

    /**
     * Remove source directory flag. Removes the source directory from the Maven build path.
     * Has no effect unless {@link #addOutputDirectory} is true.
     */
    @Parameter(property = "lombok.removeSourceDirectory", defaultValue = "false", required = true)
    protected boolean removeSourceDirectory;

    /**
     * Add found artifacts to classpath.
     */
    @Parameter(property = "lombok.addToClasspath", defaultValue = "true", required = true)
    protected boolean addToClasspath;

    /**
     * Formatting preferences.
     */
    @Parameter
    protected Map<String, String> formatPreferences;

    /**
     * The Maven project to act upon.
     */
    @Parameter(property="project", required=true, readonly=true)
    protected MavenProject project;

    /**
     * The plugin dependencies.
     */
    @Parameter(property="plugin.artifacts", required=true, readonly=true)
    private List<Artifact> pluginArtifacts;

    @Parameter(property="plugin", required=true, readonly=true)
    protected PluginDescriptor pluginDescriptor;

    /**
     * Build Context for improved Maven-Eclipse integration.
     */
    @Component
    private BuildContext buildContext;

    protected abstract String getGoalDescription ();

    protected abstract File getOutputDirectory();

    protected abstract File getSourceDirectory();

    protected abstract String getSourcePath();

    protected abstract void addSourceRoot(String path);

    protected abstract void removeSourceFromRoot() throws IOException;

    @Override
    public void execute() throws MojoExecutionException {
        final Log logger = getLog();
        assert null != logger;

        final String goal = getGoalDescription();
        logger.debug("Starting " + goal);
        final File outputDirectory = getOutputDirectory();
        logger.debug("outputDirectory: " + outputDirectory);
        final File sourceDirectory = getSourceDirectory();
        logger.debug("sourceDirectory: " + sourceDirectory);
        final String sourcePath = getSourcePath();
        logger.debug("sourcePath: " + sourcePath);

        if (this.skip) {
            logger.warn("Skipping " + goal);
        } else if (sourceDirectory.exists()) {
            // Build a classPath for delombok...
            final StringBuilder classPathBuilder = new StringBuilder();
            for (final Object artifact : project.getArtifacts()) {
                classPathBuilder.append(((Artifact)artifact).getFile()).append(File.pathSeparatorChar);
            }
            for (final Artifact artifact : pluginArtifacts) {
                classPathBuilder.append(artifact.getFile()).append(File.pathSeparatorChar);
            }
            // delombok needs tools.jar (prior to Java 9)...
            if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9)) {
                final String javaHome = System.getProperty("java.home");
                final File toolsJar = new File (javaHome,
                    ".." + File.separatorChar + "lib" + File.separatorChar + "tools.jar");
                if (toolsJar.exists()) {
                    try {
                        pluginDescriptor.getClassRealm().addURL(toolsJar.toURI().toURL());
                    } catch (final IOException e) {
                        logger.warn("Unable to add tools.jar; " + toolsJar);
                    }
                } else {
                    logger.warn("Unable to detect tools.jar; java.home is " + javaHome);
                }
            }
            final String classPath = classPathBuilder.toString();
            logger.debug("classpath: " + classPath);
            try {
                final Delombok delombok = new Delombok();
                delombok.setVerbose(this.verbose);
                if (addToClasspath) delombok.setClasspath(classPath);

                if (StringUtils.isNotBlank(this.encoding)) {
                    try {
                        delombok.setCharset(this.encoding);
                    } catch (final UnsupportedCharsetException e) {
                        logger.error("The encoding parameter is invalid; Please check!", e);
                        throw new MojoExecutionException("Unknown charset: " + this.encoding, e);
                    }
                } else {
                    logger.warn("No encoding specified; using default: " + Charset.defaultCharset());
                }

                if (null != formatPreferences && !formatPreferences.isEmpty()) {
                    try {
                        // Construct a list array just like the command-line option...
                        final List<String> formatOptions = new ArrayList<String>(formatPreferences.size());
                        for (final Map.Entry<String, String> entry : formatPreferences.entrySet()) {
                            final String key = entry.getKey();
                            // "pretty" is an exception -- it has no value...
                            formatOptions.add( "pretty".equalsIgnoreCase(key) ? key : (key + ':' + entry.getValue()) );
                        }
                        delombok.setFormatPreferences(delombok.formatOptionsToMap(formatOptions));
                    } catch (final Exception e) {
                        logger.error("The formatPreferences parameter is invalid; Please check!", e);
                        throw new MojoExecutionException("Invalid formatPreferences: " + this.formatPreferences, e);
                    }
                }

                try {
                    delombok.setOutput(outputDirectory);
                    delombok.setSourcepath(getSourcePath());
                    delombok.addDirectory(sourceDirectory);
                    if (buildContext.hasDelta(sourceDirectory)) {
                        delombok.delombok();
                        logger.info(goal + " complete.");

                        if (this.addOutputDirectory) {
                            // adding generated sources to Maven project
                            addSourceRoot(outputDirectory.getCanonicalPath());
                            // Notify build context about a file created, updated or deleted...
                            if (this.removeSourceDirectory)
                            {
                                removeSourceFromRoot();
                            }
                            buildContext.refresh(outputDirectory);
                        }
                    } else {
                        logger.info(goal + " skipped; No deltas detected.");
                    }
                } catch (final IOException e) {
                    logger.error("Unable to delombok!", e);
                    throw new MojoExecutionException("I/O problem during delombok", e);
                }
            } catch (final ClassNotFoundException e) {
                throw new MojoExecutionException("Unable to delombok", e);
            } catch (final IllegalAccessException e) {
                throw new MojoExecutionException("Unable to delombok", e);
            } catch (final InvocationTargetException e) {
                throw new MojoExecutionException("Unable to delombok", e);
            } catch (final InstantiationException e) {
                throw new MojoExecutionException("Unable to delombok", e);
            } catch (final NoSuchMethodException e) {
                throw new MojoExecutionException("Unable to delombok", e);
            }
        } else {
            logger.warn("Skipping " + goal + "; no source to process.");
        }
    }
}
