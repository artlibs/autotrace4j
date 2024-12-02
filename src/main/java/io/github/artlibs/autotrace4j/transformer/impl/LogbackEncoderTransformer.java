package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static io.github.artlibs.autotrace4j.context.TraceContext.injectTraceId;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Logback Encoder增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class LogbackEncoderTransformer extends AbsDelegateTransformer.Instance {
    private static final String LAYOUT_WRAPPING_ENCODER
            = "ch.qos.logback.core.encoder.LayoutWrappingEncoder";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named(LAYOUT_WRAPPING_ENCODER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("encode").and(
                isOverriddenFrom(named(LAYOUT_WRAPPING_ENCODER)).or(
                isDeclaredBy(named(LAYOUT_WRAPPING_ENCODER)) )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) {
        if (Objects.isNull(result) || !(result instanceof byte[])) {
            return result;
        }
        return injectTraceId(new String((byte[])result)).getBytes();
    }
}
