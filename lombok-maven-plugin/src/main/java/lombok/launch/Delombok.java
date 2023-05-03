package lombok.launch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Since the Shadow Class Loader hides Lombok's internal Delombok, we need to access it via reflection.
 *
 * @see <a href="https://github.com/rzwitserloot/lombok/blob/master/src/delombok/lombok/delombok/Delombok.java">lombok.delombok.Delombok</a>
 */
public class Delombok {

    private final Object delombokInstance;

    private final Method addDirectory;
    private final Method delombok;
    private final Method formatOptionsToMap;
    private final Method setVerbose;
    private final Method setCharset;
    private final Method setClasspath;
    private final Method setModulepath;
    private final Method setFormatPreferences;
    private final Method setOutput;
    private final Method setSourcepath;

    public Delombok () throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        final ClassLoader shadowClassLoader = Main.getShadowClassLoader();
        final Class<?> delombokClass = shadowClassLoader.loadClass("lombok.delombok.Delombok");
        this.delombokInstance = delombokClass.getDeclaredConstructor().newInstance();
        // Get method handles...
        this.addDirectory = delombokClass.getMethod("addDirectory", File.class);
        this.delombok = delombokClass.getMethod("delombok");
        this.formatOptionsToMap = delombokClass.getMethod("formatOptionsToMap", List.class);
        this.setVerbose = delombokClass.getMethod("setVerbose", boolean.class);
        this.setCharset = delombokClass.getMethod("setCharset", String.class);
        this.setClasspath = delombokClass.getMethod("setClasspath", String.class);
        this.setModulepath = delombokClass.getMethod("setModulepath", String.class);
        this.setFormatPreferences = delombokClass.getMethod("setFormatPreferences", Map.class);
        this.setOutput = delombokClass.getMethod("setOutput", File.class);
        this.setSourcepath = delombokClass.getMethod("setSourcepath", String.class);
    }

    public void addDirectory (final File base) throws IllegalAccessException, IOException, InvocationTargetException {
        addDirectory.invoke(delombokInstance, base);
    }

    public boolean delombok () throws IllegalAccessException, IOException, InvocationTargetException {
        return Boolean.parseBoolean( delombok.invoke(delombokInstance).toString() );
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> formatOptionsToMap (final List<String> formatOptions) throws Exception {
        return (Map<String, String>)formatOptionsToMap.invoke(null, formatOptions);
    }

    public void setVerbose (final boolean verbose) throws IllegalAccessException, InvocationTargetException {
        setVerbose.invoke(delombokInstance, verbose);
    }

    public void setCharset (final String charset) throws IllegalAccessException, InvocationTargetException {
        setCharset.invoke(delombokInstance, charset);
    }

    public void setClasspath (final String classpath) throws IllegalAccessException, InvocationTargetException {
        setClasspath.invoke(delombokInstance, classpath);
    }

    public void setModulepath (final String modulepath) throws IllegalAccessException, InvocationTargetException {
        setModulepath.invoke(delombokInstance, modulepath);
    }

    public void setFormatPreferences (final Map<String, String> prefs) throws IllegalAccessException, InvocationTargetException {
        setFormatPreferences.invoke(delombokInstance, prefs);
    }

    public void setOutput (final File dir) throws IllegalAccessException, InvocationTargetException {
        setOutput.invoke(delombokInstance, dir);
    }

    public void setSourcepath (final String sourcepath) throws IllegalAccessException, InvocationTargetException {
        setSourcepath.invoke(delombokInstance, sourcepath);
    }
}
