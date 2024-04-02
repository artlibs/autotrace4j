package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.interceptor.base.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.ctx.MethodWrapper;
import com.github.artlibs.autotrace4j.ctx.ReflectUtils;
import com.github.artlibs.autotrace4j.utils.Constants;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Apache HttpComponents client
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class ApacheHttpClientInterceptor extends AbstractInstanceInterceptor {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.apache.http.impl.client.AbstractHttpClient")
                .or(ElementMatchers.named("org.apache.http.impl.client.MinimalHttpClient"))
                .or(ElementMatchers.named("org.apache.http.impl.client.InternalHttpClient"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("doExecute");
    }
}
