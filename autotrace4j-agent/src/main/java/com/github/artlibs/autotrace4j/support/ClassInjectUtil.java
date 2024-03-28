package com.github.artlibs.autotrace4j.support;

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
 * 类注入util
 *
 * @author suopo
 */
public class ClassInjectUtil {

    /**
     *
     * @param instrumentation
     * @param packagePrefix
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void injectClassToBootStrapClassLoader(
        Instrumentation instrumentation,
        String packagePrefix
    ) throws IOException, URISyntaxException {
        // find the class file, and don't load it.
        Enumeration<URL> classesEnumeration = ClassInjectUtil.class
            .getClassLoader()
            .getResources(packagePrefix.replaceAll("\\.", "/") + "/");
        Map<String, byte[]> classes = new HashMap<>();
        ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(ClassInjectUtil.class.getClassLoader());
        while (classesEnumeration.hasMoreElements()) {
            URL packageDirUrl = classesEnumeration.nextElement();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(packageDirUrl.toURI()), "*.class")) {
                for (Path path : paths) {
                    String canonicalName = buildCanonicalName(packagePrefix, path.getFileName().toString());
                    classes.put(canonicalName, classFileLocator.locate(canonicalName).resolve());
                }
            }
        }
        File classInjectTempDir = SystemUtil.getClassInjectTempDir(CommonConstant.INJECT_DIR_BOOTSTRAP);
        // inject classes to bootstrap loader
        ClassInjector
            .UsingInstrumentation.of(classInjectTempDir, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
            .injectRaw(classes);
    }

    private static String buildCanonicalName(String packagePrefixes, String fileName) {
        return packagePrefixes + "." + fileName.replace(".class", "");
    }

}
