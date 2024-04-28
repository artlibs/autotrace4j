package io.github.artlibs.autotrace4j.interceptor;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;


/**
 * Transform Listener
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class AutoListener implements AgentBuilder.Listener {
    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }

    @Override
    public void onTransformation(
        TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType
    ) {
        // NO Sonar
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        System.err.println("TypeName: " + typeName + "\nclassLoader: " + classLoader
                + "\nmodule: " + module + "\nthrowable: " + throwable);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }
}
