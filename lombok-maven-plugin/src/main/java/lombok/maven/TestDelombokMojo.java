package lombok.maven;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
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
      return StringUtils.joinWith(File.pathSeparator,
          StringUtils.join(this.project.getCompileSourceRoots(), File.pathSeparatorChar),
          StringUtils.join(this.project.getTestCompileSourceRoots(), File.pathSeparatorChar)
      );
    }

    @Override
    protected void addSourceRoot(final String path) {
        project.addTestCompileSourceRoot(path);
    }

    @Override
    protected void removeSourceFromRoot() throws IOException
    {
        final Log logger = getLog();
        assert null != logger;
        List<String> roots = project.getCompileSourceRoots();
        if (roots == null)
        {
            logger.warn("roots are null");
        }
        else
        {
            Iterator<String> i = roots.iterator();
            while (i.hasNext())
            {
                String root = i.next();
                if (sourceDirectory.getName().equals(root))
                {
                    i.remove();
                    logger.info("removing root: " + root);
                }
                else if (sourceDirectory.getCanonicalFile().getName().equals(root))
                {
                    i.remove();
                    logger.info("removing root: " + root);
                }
                else
                {
                    logger.debug("ignoring root: " + root);
                }
            }
        }
        roots = project.getTestCompileSourceRoots();
        if (roots == null)
        {
            logger.warn("test roots are null");
        }
        else
        {
            Iterator<String> i = roots.iterator();
            while (i.hasNext())
            {
                String root = i.next();
                if (sourceDirectory.getName().equals(root))
                {
                    i.remove();
                    logger.info("removing root: " + root);
                }
                else if (sourceDirectory.getCanonicalFile().getName().equals(root))
                {
                    i.remove();
                    logger.info("removing root: " + root);
                }
                else
                {
                    logger.debug("ignoring root: " + root);
                }
            }
        }
    }
}
