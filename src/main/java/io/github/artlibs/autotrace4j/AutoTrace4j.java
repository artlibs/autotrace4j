package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.interceptor.Transformer;
import io.github.artlibs.autotrace4j.support.ClassUtils;
import io.github.artlibs.autotrace4j.support.JavaModuleUtils;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.Objects;

import static io.github.artlibs.autotrace4j.support.JavaModuleUtils.getOwnModule;
import static io.github.artlibs.autotrace4j.support.JavaModuleUtils.notJavaModule;

/**
 * Auto Trace for Java
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class AutoTrace4j {
    private AutoTrace4j(){}

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
                "Please specify your java package name prefix (Agent parameter)" +
                    " to determine the enhancement scope; such as：\n"
                    + "-javaagent:/dir/to/autotrace4j.jar=com.your-domain1.pkg1,com.your-domain2.pkg2");
        }
        String ctxPackagePrefix = AutoTrace4j.class.getPackage().getName() + ".context";
        ClassUtils.injectClassToBootStrap(instrument, ctxPackagePrefix);
        //note: this method must be called after injectClassToBootStrap, don't move it forward
        compatibleJavaModule(ctxPackagePrefix);
        // do intercept
        Transformer.intercept(enhancePackages).on(instrument);
    }

    private static void compatibleJavaModule(String ctxPackagePrefix) {
        if (notJavaModule()) {
            return;
        }

        final String[] agentNecessaryJavaBasePackages = {
                "sun.net.www.protocol.http",
                "sun.net.www",
                "jdk.internal.loader",
        };

        // java9+: open the system module to us
        JavaModuleUtils.openJavaBaseModuleForAnotherModule(
                agentNecessaryJavaBasePackages,
                JavaModule.ofType(JavaModuleUtils.MethodLockSupport.class)
        );
        // java9+: remove the package - module mapping to avoid double context package's class
        // note: this method need the privilege of 'jdk.internal.loader' package
        JavaModuleUtils.removePkgModuleMapping(new String[]{ ctxPackagePrefix });
        // java9+: open the system module to bootstrap's unnamed module
        // we need found the unnamed module by ModuleLocator
        JavaModuleUtils.openJavaBaseModuleForAnotherModule(
                agentNecessaryJavaBasePackages,
                getOwnModule(ctxPackagePrefix + ".jdk.ModuleLocator")
        );
    }

}
