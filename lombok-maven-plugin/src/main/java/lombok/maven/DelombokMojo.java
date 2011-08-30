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
 * Delombok java source with lombok annotations.
 *
 * @goal delombok
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @threadSafe
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
public class DelombokMojo extends AbstractMojo {

    /**
     * Specifies whether the delombok generation should be skipped.
     * @parameter expression="${lombok.delombok.skip}" default-value="false"
     * @required
     */
    private boolean skip;

    /**
     * Encoding.
     * @parameter expression="${lombok.encoding}" default-value="${project.build.sourceEncoding}"
     * @required
     */
    private String encoding;

    /**
     * Location of the lombok annotated source files.
     * @parameter expression="${lombok.sourceDirectory}" default-value="${project.basedir}/src/main/lombok"
     * @required
     */
    private File sourceDirectory;

    /**
     * Location of the java source files.
     * @parameter expression="${lombok.sourcePath}" default-value="${project.basedir}/src/main/java"
     * @required
     */
    private File sourcePath;

    /**
     * Location of the generated source files.
     * @parameter expression="${lombok.outputDirectory}" default-value="${project.build.directory}/generated-sources/delombok"
     * @required
     */
    private File outputDirectory;

    /**
     * Verbose flag.  Print the name of each file as it is being delombok-ed.
     * @parameter expression="${lombok.verbose}" default-value="false"
     * @required
     */
    private boolean verbose;

    /**
     * The Maven project to act upon.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The plugin dependencies.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List<Artifact> pluginArtifacts;

    @Override
    public void execute() throws MojoExecutionException {
        final Log logger = getLog();
        assert null != logger;

        if (this.skip) {
            logger.warn("Skipping delombok.");
        } else if (this.sourceDirectory.exists()) {
            // Build a classPath for delombok...
            final StringBuilder classPathBuilder = new StringBuilder();
            for (final Object artifact : project.getArtifacts()) {
                classPathBuilder.append(((Artifact)artifact).getFile()).append(File.pathSeparatorChar);
            }
            for (final Artifact artifact : pluginArtifacts) {
                classPathBuilder.append(artifact.getFile()).append(File.pathSeparatorChar);
            }
            final String classPath = classPathBuilder.toString();
            logger.debug("Delombok classpath = " + classPath);
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
                delombok.setOutput(this.outputDirectory);
                delombok.setSourcepath(this.sourcePath.getCanonicalPath());
                delombok.addDirectory(this.sourceDirectory);
                delombok.delombok();
                logger.info("Delombok complete.");

                // adding generated sources to Maven project
                project.addCompileSourceRoot(outputDirectory.getCanonicalPath());
            } catch (final IOException e) {
                logger.error("Unable to delombok!", e);
                throw new MojoExecutionException("I/O problem during delombok", e);
            }
        } else {
            logger.warn("Skipping delombok; no source to process.");
        }
    }
}
