package lombok.maven;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * Delombok java test source with lombok annotations.
 *
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
@Mojo(name="testDelombok", defaultPhase=LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution=ResolutionScope.TEST, threadSafe=true)
public class TestDelombokMojo extends AbstractDelombokMojo {

    /**
     * Location of the lombok annotated source files.
     */
    @Parameter(property="lombok.testSourceDirectory", defaultValue="${project.basedir}/src/test/lombok", required=true)
    private File sourceDirectory;

    /**
     * Location of the generated source files.
     */
    @Parameter(property="lombok.testOutputDirectory", defaultValue="${project.build.directory}/generated-test-sources/delombok", required=true)
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
