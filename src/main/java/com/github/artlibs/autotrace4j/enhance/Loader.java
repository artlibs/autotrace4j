package com.github.artlibs.autotrace4j.enhance;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import com.github.artlibs.autotrace4j.enhance.interceptor.Interceptor;
import com.github.artlibs.autotrace4j.enhance.interceptor.impl.CallbackInterfaceInterceptor;
import com.github.artlibs.autotrace4j.support.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
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
    public static List<Interceptor> load() throws IOException, URISyntaxException {
        if (Objects.nonNull(interceptorList) && !interceptorList.isEmpty()) {
            return interceptorList;
        }

        interceptorList = new ArrayList<>(16);

        ClassUtils.walkClassFiles((path, classCanonicalName) -> {
            try {
                Class<?> clazz = Class.forName(classCanonicalName);
                if (!clazz.getSimpleName().equals("CallbackInterfaceInterceptor") && Interceptor.class.isAssignableFrom(clazz)) {
                    interceptorList.add((Interceptor) clazz.newInstance());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, AutoTrace4j.class.getPackage().getName() + ".enhance.interceptor.impl");

        return interceptorList;
    }

}
