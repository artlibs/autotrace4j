package io.github.artlibs.autotrace4j.interceptor;

import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AutoListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTransformation(
        TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType
    ) {
        // NO Sonar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        logger.error("TypeName: " + typeName + "\nclassLoader: " + classLoader
                + "\nmodule: " + module + "\nthrowable: " + throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        // NO Sonar
    }
}
