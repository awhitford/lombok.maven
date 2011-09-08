package lombok.maven;

import java.io.File;

import org.apache.commons.lang3.StringUtils;


/**
 * Delombok java test source with lombok annotations.
 *
 * @goal testDelombok
 * @phase generate-test-sources
 * @requiresDependencyResolution test
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
public class TestDelombokMojo extends AbstractDelombokMojo {

    /**
     * Location of the lombok annotated source files.
     * @parameter expression="${lombok.testSourceDirectory}" default-value="${project.basedir}/src/test/lombok"
     * @required
     */
    private File sourceDirectory;

    /**
     * Location of the generated source files.
     * @parameter expression="${lombok.testOutputDirectory}" default-value="${project.build.directory}/generated-test-sources/delombok"
     * @required
     */
    private File outputDirectory;

    @Override
    protected String getGoalDescription() {
        return "Test Delombok";
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected File getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    protected String getSourcePath() {
        return StringUtils.join(this.project.getTestCompileSourceRoots(), File.pathSeparatorChar);
    }

    @Override
    protected void addSourceRoot(final String path) {
        project.addTestCompileSourceRoot(path);
    }
}
