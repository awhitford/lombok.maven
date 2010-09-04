package lombok.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;

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
     * Location of the generated source files.
     * @parameter expression="${lombok.outputDirectory}" default-value="${project.build.directory}/generated-sources/delombok"
     * @required
     */
    private File outputDirectory;

    /**
     * Verbose flag.
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

    @Override
    public void execute() throws MojoExecutionException {
        final Log logger = getLog();
        assert null != logger;

        if (this.skip) {
            logger.warn("Skipping delombok.");
        } else if (this.sourceDirectory.exists()) {
            final Delombok delombok = new Delombok();
            delombok.setVerbose(this.verbose);
            try {
                delombok.setCharset(this.encoding);
            } catch (final UnsupportedCharsetException e) {
                logger.error("The encoding parameter is invalid; Please check!", e);
                throw new MojoExecutionException("Unknown charset: " + this.encoding, e);
            }

            try {
                delombok.setOutput(this.outputDirectory);
                delombok.delombok(this.sourceDirectory);
                logger.info("Delombok complete.");

                // adding generated sources to Maven project
                project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

            } catch (final IOException e) {
                logger.error("Unable to delombok!", e);
                throw new MojoExecutionException("I/O problem during delombok", e);
            }
        } else {
            logger.warn("Skipping delombok; no source to process.");
        }
    }
}
