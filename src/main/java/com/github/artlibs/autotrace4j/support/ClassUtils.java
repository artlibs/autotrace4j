package com.github.artlibs.autotrace4j.support;

import com.github.artlibs.autotrace4j.exception.WalkClassFileException;
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
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 功能：类utils
 *
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class ClassUtils {
    private ClassUtils() {}

    /**
     * 注入指定包名下的class到bootstrap
     *
     * @param instrumentation instrumentation对象
     * @param packagePrefix   包前缀
     * @return {@link Instrumentation}
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static Instrumentation injectClassToBootStrap(
        Instrumentation instrumentation, String packagePrefix
    ) throws IOException, URISyntaxException {
        Map<String, byte[]> classes = new HashMap<>();
        ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(ClassUtils.class.getClassLoader());
        walkClassFiles((path, classCanonicalName) -> {
            try {
                classes.put(classCanonicalName, classFileLocator.locate(classCanonicalName).resolve());
            } catch (IOException e) {
                throw new WalkClassFileException(e);
            }
        }, packagePrefix);
        File classInjectTempDir = SystemUtils.getClassInjectTempDir(Constants.INJECT_DIR_BOOTSTRAP);
        ClassInjector
            .UsingInstrumentation.of(classInjectTempDir, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
            .injectRaw(classes);
        return instrumentation;
    }

    /**
     * walk 类文件
     *
     * @param walker        处理所有被找到的class
     * @param packagePrefix 包前缀
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static void walkClassFiles(
        BiConsumer<Path, String> walker,
        String packagePrefix
    ) throws IOException, URISyntaxException {
        Enumeration<URL> classesEnumeration = ClassUtils.class
            .getClassLoader()
            .getResources(packagePrefix.replace(".", "/"));
        while (classesEnumeration.hasMoreElements()) {
            URL packageDirUrl = classesEnumeration.nextElement();
            Path packagePath;
            FileSystem zipFileSystem = null;
            try {
                if (packageDirUrl.getProtocol().equals("jar")) {
                    zipFileSystem = FileSystems.newFileSystem(packageDirUrl.toURI(), new HashMap<>());
                    String uriStr = packageDirUrl.toURI().toString();
                    packagePath = zipFileSystem.getPath(uriStr.substring(uriStr.indexOf("!") + 1));
                } else {
                    packagePath = Paths.get(packageDirUrl.toURI());
                }
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(packagePath, "*.class")) {
                    for (Path path : paths) {
                        walker.accept(path, buildCanonicalName(packagePrefix, path.getFileName().toString()));
                    }
                }
            } finally {
                if (Objects.nonNull(zipFileSystem)) {
                    zipFileSystem.close();
                }
            }
        }
    }

    /**
     * 生成规范名称
     *
     * @param packagePrefixes 包前缀
     * @param classFileName   类文件名
     * @return {@link String}
     */
    private static String buildCanonicalName(String packagePrefixes, String classFileName) {
        return packagePrefixes + "." + classFileName.replace(".class", "");
    }

}