package com.github.artlibs.autotrace4j.enhance;

import com.github.artlibs.autotrace4j.enhance.interceptor.Interceptor;
import com.github.artlibs.autotrace4j.enhance.interceptor.impl.CallbackInterfaceInterceptor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 功能：Interceptor Loader
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public final class Loader {
    private static List<Interceptor> interceptorList = null;

    private Loader() {}

    /**
     * 加载impl子包下的所有增强器并实例化
     * @return list of Enhancers
     * @throws IOException -
     * @throws ClassNotFoundException -
     * @throws IllegalAccessException -
     * @throws InstantiationException -
     */
    public static List<Interceptor> load() throws IOException, ClassNotFoundException,
                                                  IllegalAccessException, InstantiationException {
        if (Objects.nonNull(interceptorList) && !interceptorList.isEmpty()) {
            return interceptorList;
        }

        interceptorList = new ArrayList<>(16);
        List<JarEntry> classFiles = walkClassFiles();
        for (JarEntry classFile : classFiles) {
            String className = classFile.getName();
            if (className.contains(CallbackInterfaceInterceptor.class.getSimpleName())) {
                continue;
            }
            className = className.replace(File.separator, ".").substring(0, className.length() - 6);
            interceptorList.add((Interceptor)Class.forName(className).newInstance());
        }

        return interceptorList;
    }

    /**
     * Walk the class files
     * @return list of JarEntry
     * @throws IOException -
     */
    private static List<JarEntry> walkClassFiles() throws IOException {
        List<JarEntry> classFileList = new ArrayList<>(16);
        String basePackage = (Loader.class.getPackage().getName()
                + ".interceptor.impl").replace('.', File.separatorChar);
        Enumeration<URL> resources = Thread.currentThread()
                .getContextClassLoader().getResources(basePackage);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (!"jar".equals(resource.getProtocol())) {
                continue;
            }

            JarFile jar = ((JarURLConnection)resource.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                // xxx/impl/XXX.class
                String name = entry.getName();
                if (name.startsWith(basePackage) && name.endsWith(".class") && !name.contains("$")) {
                    classFileList.add(entry);
                }
            }
        }
        return classFileList;
    }
}
