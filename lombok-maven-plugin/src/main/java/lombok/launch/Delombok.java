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

    private static final ClassLoader SHADOW_CLASS_LOADER = Main.createShadowClassLoader();
    private final Class<?> delombokClass;
    private final Object delombok;

    private final Method addDirectory;
    private final Method delombokMethod;
    private final Method formatOptionsToMap;
    private final Method setVerbose;
    private final Method setCharset;
    private final Method setClasspath;
    private final Method setFormatPreferences;
    private final Method setOutput;
    private final Method setSourcepath;

    public Delombok () throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        this.delombokClass = SHADOW_CLASS_LOADER.loadClass("lombok.delombok.Delombok");
        this.delombok = delombokClass.newInstance();
        // Get method handles...
        this.addDirectory = delombokClass.getMethod("addDirectory", File.class);
        this.delombokMethod = delombokClass.getMethod("delombok");
        this.formatOptionsToMap = delombokClass.getMethod("formatOptionsToMap", List.class);
        this.setVerbose = delombokClass.getMethod("setVerbose", boolean.class);
        this.setCharset = delombokClass.getMethod("setCharset", String.class);
        this.setClasspath = delombokClass.getMethod("setClasspath", String.class);
        this.setFormatPreferences = delombokClass.getMethod("setFormatPreferences", Map.class);
        this.setOutput = delombokClass.getMethod("setOutput", File.class);
        this.setSourcepath = delombokClass.getMethod("setSourcepath", String.class);
    }

    public void addDirectory (final File base) throws IllegalAccessException, IOException, InvocationTargetException {
        addDirectory.invoke(delombok, base);
    }

    public boolean delombok () throws IllegalAccessException, IOException, InvocationTargetException {
        return Boolean.parseBoolean( delombokMethod.invoke(delombok).toString() );
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> formatOptionsToMap (final List<String> formatOptions) throws Exception {
        return (Map<String, String>)formatOptionsToMap.invoke(null, formatOptions);
    }

    public void setVerbose (final boolean verbose) throws IllegalAccessException, InvocationTargetException {
        setVerbose.invoke(delombok, verbose);
    }

    public void setCharset (final String charset) throws IllegalAccessException, InvocationTargetException {
        setCharset.invoke(delombok, charset);
    }

    public void setClasspath (final String classpath) throws IllegalAccessException, InvocationTargetException {
        setClasspath.invoke(delombok, classpath);
    }

    public void setFormatPreferences (final Map<String, String> prefs) throws IllegalAccessException, InvocationTargetException {
        setFormatPreferences.invoke(delombok, prefs);
    }

    public void setOutput (final File dir) throws IllegalAccessException, InvocationTargetException {
        setOutput.invoke(delombok, dir);
    }

    public void setSourcepath (final String sourcepath) throws IllegalAccessException, InvocationTargetException {
        setSourcepath.invoke(delombok, sourcepath);
    }
}

