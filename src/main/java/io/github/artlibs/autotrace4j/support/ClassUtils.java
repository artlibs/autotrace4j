package io.github.artlibs.autotrace4j.support;

import io.github.artlibs.autotrace4j.exception.WalkClassFileException;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static io.github.artlibs.autotrace4j.support.Constants.*;

/**
 * 功能：类utils
 *
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class ClassUtils {
    private ClassUtils() {}

    /**
     * 注入指定包名下的class到bootstrap
     *
     * @param instrumentation     instrumentation对象
     * @param packagePrefix       包前缀
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static void injectClassToBootStrap(
        Instrumentation instrumentation, String packagePrefix
    ) throws IOException, URISyntaxException {
        Map<String, byte[]> classes = new HashMap<>();
        try(ClassFileLocator classFileLocator = ClassFileLocator
                .ForClassLoader.of(ClassUtils.class.getClassLoader())) {
            walkClassFiles((path, classCanonicalName) -> {
                try {
                    classes.put(classCanonicalName, classFileLocator.locate(classCanonicalName).resolve());
                } catch (IOException e) {
                    throw new WalkClassFileException(e);
                }
            }, packagePrefix, true);
        }
        File classInjectTempDir = SystemUtils.getClassInjectTempDir(Constants.INJECT_DIR_BOOTSTRAP);
        ClassInjector
            .UsingInstrumentation.of(classInjectTempDir, ClassInjector
                        .UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
            .injectRaw(classes);
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
        String packagePrefix,
        boolean ignoreRepeat
    ) throws IOException, URISyntaxException {
        Enumeration<URL> classesEnumeration = ClassUtils.class
            .getClassLoader()
            .getResources(packagePrefix.replace(DOT, SLASH));
        while (classesEnumeration.hasMoreElements()) {
            URL packageDirUrl = classesEnumeration.nextElement();
            Path packagePath;
            FileSystem zipFileSystem = null;
            try {
                if (packageDirUrl.getProtocol().equals(JAR)) {
                    zipFileSystem = FileSystems.newFileSystem(packageDirUrl.toURI(), new HashMap<>());
                    String uriStr = packageDirUrl.toURI().toString();
                    packagePath = zipFileSystem.getPath(uriStr.substring(uriStr.indexOf("!") + 1));
                } else {
                    packagePath = Paths.get(packageDirUrl.toURI());
                }
                Files.walkFileTree(packagePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.getFileName().toString().endsWith(DOT_CLASS)){
                            String separator = file.getFileSystem().getSeparator();
                            String replaced = file.toString().replace(separator, DOT);
                            String classCanonicalName = replaced.substring(replaced.indexOf(packagePrefix)).replaceAll(".class", "");
                            walker.accept(file, classCanonicalName);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } finally {
                if (Objects.nonNull(zipFileSystem)) {
                    zipFileSystem.close();
                }
            }
            if (ignoreRepeat){
                break;
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
    @SuppressWarnings("unused")
    private static String buildCanonicalName(String packagePrefixes, String classFileName) {
        return packagePrefixes + DOT + classFileName.replace(DOT_CLASS, EMPTY);
    }

    /**
     * find the method from class or super class
     *
     * @param clazz          clazz
     * @param methodName     methodName
     * @param parameterTypes parameterTypes
     * @return {@link Method} or null if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        while (clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                break;
            } catch (NoSuchMethodException ignored) {
                // NO Sonar
            }
            clazz = clazz.getSuperclass();
        }
        if (method != null) {
            method.setAccessible(true);
        }

        return method;
    }

}
