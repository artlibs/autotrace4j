package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.MethodWrapper;
import com.github.artlibs.autotrace4j.context.ReflectUtils;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * RocketMq Listener Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class RocketMqListenerInterceptor extends AbstractInstanceInterceptor {
    private static final String GUP = "getUserProperty";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[0], GUP, String.class);

        String traceId = methodWrapper.invoke(AutoTraceCtx.TRACE_KEY);
        String parentSpanId = methodWrapper.invoke(AutoTraceCtx.SPAN_KEY);

        if (Objects.isNull(traceId)) {
            traceId = AutoTraceCtx.generate();
        }
        AutoTraceCtx.setTraceId(traceId);
        AutoTraceCtx.setParentSpanId(parentSpanId);
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("doConvertMessage");
    }
}
