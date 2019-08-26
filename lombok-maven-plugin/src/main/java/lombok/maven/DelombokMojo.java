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
 * Delombok java source with lombok annotations.
 *
 * @author <a href="mailto:anthony@whitford.com">Anthony Whitford</a>
 * @see <a href="http://projectlombok.org/features/delombok.html">Delombok</a>
 */
@Mojo(name="delombok", defaultPhase=LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution=ResolutionScope.COMPILE, threadSafe=true)
public class DelombokMojo extends AbstractDelombokMojo {

    /**
     * Location of the lombok annotated source files.
     */
    @Parameter(property="lombok.sourceDirectory", defaultValue="${project.basedir}/src/main/lombok", required=true)
    private File sourceDirectory;

    /**
     * Location of the generated source files.
     */
    @Parameter(property="lombok.outputDirectory", defaultValue="${project.build.directory}/generated-sources/delombok", required=true)
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

    @Override
    protected void removeSourceFromRoot() throws IOException
    {
        final Log logger = getLog();
        assert null != logger;
        logger.debug("seeking to remove sourceDirectory: " + sourceDirectory.getCanonicalPath());
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
                if (sourceDirectory.getCanonicalPath().equals(root))
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
