package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Logback/Log4j - LoggingEvent 增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class LogbackLog4jEventTransformer extends AbsVisitorTransformer.AbsConstructor {
    /**
     * 只往类注入属性
     * <p>
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("ch.qos.logback.classic.spi.LoggingEvent")
                .or(named("org.apache.log4j.spi.LoggingEvent"));
    }

}
