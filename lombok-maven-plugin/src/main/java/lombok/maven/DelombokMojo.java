package lombok.maven;

import java.io.File;

import org.apache.commons.lang3.StringUtils;


/**
 * Delombok java source with lombok annotations.
 *
 * @goal delombok
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
public class DelombokMojo extends AbstractDelombokMojo {

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

    @Override
    protected String getGoalDescription() {
        return "Delombok";
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
        return StringUtils.join(this.project.getCompileSourceRoots(), File.pathSeparatorChar);
    }

    @Override
    protected void addSourceRoot(final String path) {
        project.addCompileSourceRoot(path);
    }
}
