package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * OkHttp Client Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class OkHttpClientInterceptor extends AbstractInstanceInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) {
        final String traceId = AutoTraceCtx.getTraceId();
        if (Objects.isNull(traceId) || Objects.isNull(thiz)) {
            return;
        }
        List<String> namesAndValues = ReflectUtils.getFieldValue(thiz, "namesAndValues", true);
        if (Objects.nonNull(namesAndValues) && !namesAndValues.contains(AutoTraceCtx.ATO_TRACE_ID)) {
            namesAndValues.add(AutoTraceCtx.ATO_TRACE_ID);
            namesAndValues.add(traceId);

            final String spanId = AutoTraceCtx.getSpanId();
            if (Objects.nonNull(spanId)) {
                namesAndValues.add(AutoTraceCtx.ATO_SPAN_ID);
                namesAndValues.add(spanId);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("okhttp3.Headers$Builder");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("build");
    }
}
