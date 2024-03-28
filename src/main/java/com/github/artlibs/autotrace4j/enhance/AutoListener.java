package com.github.artlibs.autotrace4j.enhance;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.util.Arrays;
import java.util.Objects;


/**
 * 功能：监听器
 *
 * @author Fury
 * @since 2023-01-04
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
        System.err.println("TypeName: " + typeName + "\nloaded: " + loaded);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }
}
