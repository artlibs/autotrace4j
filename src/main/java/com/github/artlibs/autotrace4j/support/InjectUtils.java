package com.github.artlibs.autotrace4j.support;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 类注入utils
 *
 * @author suopo
 */
public class InjectUtils {
    private InjectUtils(){}

    /**
     * @param instrumentation -
     * @param locateClass -
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Instrumentation injectClassToBootStrap(
        Instrumentation instrumentation, Class<?> locateClass
    ) throws IOException, URISyntaxException {
        String basePackage = locateClass.getPackage().getName() + ".ctx";
        Enumeration<URL> classesEnumeration = InjectUtils.class
            .getClassLoader()
            .getResources(basePackage.replace('.', File.separatorChar));
        Map<String, byte[]> classes = new HashMap<>();
        ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(InjectUtils.class.getClassLoader());
        while (classesEnumeration.hasMoreElements()) {
            URL packageDirUrl = classesEnumeration.nextElement();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(packageDirUrl.toURI()), "*.class")) {
                for (Path path : paths) {
                    String canonicalName = buildCanonicalName(basePackage, path.getFileName().toString());
                    classes.put(canonicalName, classFileLocator.locate(canonicalName).resolve());
                }
            }
        }
        File classInjectTempDir = SystemUtils.getClassInjectTempDir(Constants.INJECT_DIR_BOOTSTRAP);
        // inject classes to bootstrap loader
        ClassInjector
            .UsingInstrumentation.of(classInjectTempDir, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
            .injectRaw(classes);
        return instrumentation;
    }

    private static String buildCanonicalName(String basePackage, String fileName) {
        return basePackage + "." + fileName.replace(".class", "");
    }

}
