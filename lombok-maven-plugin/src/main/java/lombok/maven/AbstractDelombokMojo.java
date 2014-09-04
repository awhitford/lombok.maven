package lombok.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.delombok.Delombok;
import lombok.delombok.Delombok.InvalidFormatOptionException;


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

    protected abstract String getGoalDescription ();

    protected abstract File getOutputDirectory();

    protected abstract File getSourceDirectory();

    protected abstract String getSourcePath();

    protected abstract void addSourceRoot(String path);

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
            final String classPath = classPathBuilder.toString();
            logger.debug("classpath: " + classPath);
            final Delombok delombok = new Delombok();
            delombok.setVerbose(this.verbose);
            delombok.setClasspath(classPath);

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
                } catch (final InvalidFormatOptionException e) {
                    logger.error("The formatPreferences parameter is invalid; Please check!", e);
                    throw new MojoExecutionException("Invalid formatPreferences: " + this.formatPreferences, e);
                }
            }

            try {
                delombok.setOutput(outputDirectory);
                delombok.setSourcepath(getSourcePath());
                delombok.addDirectory(sourceDirectory);
                delombok.delombok();
                logger.info(goal + " complete.");
                
                if (this.addOutputDirectory) {
                    // adding generated sources to Maven project
                    addSourceRoot(outputDirectory.getCanonicalPath());
                }
            } catch (final IOException e) {
                logger.error("Unable to delombok!", e);
                throw new MojoExecutionException("I/O problem during delombok", e);
            }
        } else {
            logger.warn("Skipping " + goal + "; no source to process.");
        }
    }
}
