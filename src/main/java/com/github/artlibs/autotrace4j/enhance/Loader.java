package com.github.artlibs.autotrace4j.enhance;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import com.github.artlibs.autotrace4j.enhance.interceptor.Interceptor;
import com.github.artlibs.autotrace4j.support.ClassUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
