package lombok.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import lombok.delombok.Delombok;


/**
 * Abstract mojo to Delombok java source with lombok annotations.
 *
 * @threadSafe
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
public abstract class AbstractDelombokMojo extends AbstractMojo {

    /**
     * Specifies whether the delombok generation should be skipped.
     * @parameter expression="${lombok.delombok.skip}" default-value="false"
     * @required
     */
    protected boolean skip;

    /**
     * Encoding.
     * @parameter expression="${lombok.encoding}" default-value="${project.build.sourceEncoding}"
     * @required
     */
    protected String encoding;

    /**
     * Verbose flag.  Print the name of each file as it is being delombok-ed.
     * @parameter expression="${lombok.verbose}" default-value="false"
     * @required
     */
    protected boolean verbose;
    
    /**
     * Add output directory flag.  Adds the output directory to the Maven build path.
     * @parameter expression="${lombok.addOutputDirectory}" default-value="true"
     * @required
     */
    protected boolean addOutputDirectory = true;

    /**
     * The Maven project to act upon.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The plugin dependencies.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List<Artifact> pluginArtifacts;

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
