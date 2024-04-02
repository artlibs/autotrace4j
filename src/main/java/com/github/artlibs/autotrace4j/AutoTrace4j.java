package com.github.artlibs.autotrace4j;

import com.github.artlibs.autotrace4j.interceptor.Transformer;
import com.github.artlibs.autotrace4j.support.ClassUtils;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Auto Trace for Java
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class AutoTrace4j {
    private AutoTrace4j() {}

    /**
     * Java Attach agentmain
     *
     * @param enhancePackages enhance packages
     * @param instrument      Instrumentation
     */
    public static void agentmain(String enhancePackages, Instrumentation instrument) throws IOException, URISyntaxException {
        premain(enhancePackages, instrument);
    }

    /**
     * Java Agent premain
     *
     * @param enhancePackages enhance packages
     * @param instrument      Instrumentation
     */
    public static void premain(String enhancePackages, Instrumentation instrument) throws IOException, URISyntaxException {
        if (Objects.isNull(enhancePackages) || enhancePackages.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Please specify the Java package name prefix (Agent parameter)" +
                        " to determine the enhancement scope; such as：\n"
                    + "-javaagent:/dir/to/autotrace4j.jar=com.your-domain.pkg1,com.your-domain.pkg2");
        }
        String ctxPackagePrefix = AutoTrace4j.class.getPackage().getName() + ".context";
        Transformer.intercept(enhancePackages)
            .on(ClassUtils.injectClassToBootStrap(instrument, ctxPackagePrefix));
    }
}
