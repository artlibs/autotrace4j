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
    private final String[] args;

    public AutoListener(String[] args) {
        this.args = Objects.isNull(args) ? new String[]{} : args;
    }

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
        System.err.println("args: " + Arrays.toString(this.args) + "\ntypeName: " + typeName + "\nloaded: " + loaded);
        //System.err.println("throwable: " + throwable.getMessage());
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }
}
