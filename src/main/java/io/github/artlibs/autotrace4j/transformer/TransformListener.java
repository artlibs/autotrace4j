package io.github.artlibs.autotrace4j.transformer;

import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.LoggerFactory;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;


/**
 * 转换过程监听器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class TransformListener implements AgentBuilder.Listener {
    private static final Logger logger = LoggerFactory.getLogger(TransformListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        logger.trace("typeName: %s\n classLoader: %s\n module: %s\nloaded: %s", typeName, classLoader, module, loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTransformation(
        TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType
    ) {
        logger.trace("typeDescription: %s\n classLoader: %s\n module: %s\nloaded: %s\ndynamicType: %s", typeDescription, classLoader, module, loaded, dynamicType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        logger.trace("typeDescription: %s\n classLoader: %s\n module: %s\nloaded: %s", typeDescription, classLoader, module, loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        logger.error("TypeName: %s\n classLoader: %s\n module: %s\n", typeName, classLoader, module, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        logger.trace("TypeName: %s\n classLoader: %s\n module: %s\nloaded: %s", typeName, classLoader, module, loaded);
    }
}
