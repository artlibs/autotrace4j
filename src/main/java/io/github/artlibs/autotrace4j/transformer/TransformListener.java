package io.github.artlibs.autotrace4j.transformer;

import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.LoggerFactory;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.utility.nullability.MaybeNull;
import net.bytebuddy.utility.nullability.NeverNull;


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
    private static final String FORMAT = "\n>>> Type: %s\n>>> ClassLoader: %s\n>>> Module: %s, Loaded: %s";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDiscovery(@NeverNull String type, @MaybeNull ClassLoader classLoader
            , @MaybeNull JavaModule module, boolean loaded) {
        logger.trace(FORMAT, type, classLoader, module, loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTransformation(@NeverNull TypeDescription type, @MaybeNull ClassLoader classLoader
            , @MaybeNull JavaModule module, boolean loaded, @NeverNull DynamicType dynamicType) {
        logger.debug(FORMAT + "\n>>> DynamicType: %s", type, classLoader, module, loaded, dynamicType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIgnored(@NeverNull TypeDescription type, @MaybeNull ClassLoader classLoader
            , @MaybeNull JavaModule module, boolean loaded) {
        logger.trace(FORMAT, type, classLoader, module, loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(@NeverNull String type, @MaybeNull ClassLoader classLoader
            , @MaybeNull JavaModule module, boolean loaded, @NeverNull Throwable throwable) {
        logger.error(FORMAT, type, classLoader, module, loaded, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onComplete(@NeverNull String type, @MaybeNull ClassLoader classLoader
            , @MaybeNull JavaModule module, boolean loaded) {
        logger.trace(FORMAT, type, classLoader, module, loaded);
    }
}
