package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import io.github.artlibs.autotrace4j.support.Constants;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Apache HttpComponents client
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ApacheHttpClientInterceptor extends AbstractInstanceInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.client.AbstractHttpClient")
                .or(named("org.apache.http.impl.client.MinimalHttpClient"))
                .or(named("org.apache.http.impl.client.InternalHttpClient"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("doExecute");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        final String traceId = AutoTraceCtx.getTraceId();
        if (Objects.nonNull(traceId)) {
            MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[1]
                    , Constants.SET_HEADER, String.class, String.class);

            methodWrapper.invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
            final String spanId = AutoTraceCtx.getSpanId();
            if (Objects.nonNull(spanId)) {
                methodWrapper.invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
            }
        }
    }
}
