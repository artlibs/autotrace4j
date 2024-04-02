package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.interceptor.base.AbstractStaticInterceptor;
import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * MDC.get("X-ATO-SPAN-ID") return SpanId
 * MDC.get("X-ATO-P-SPAN-ID") return ParentSpanId
 * MDC.get("X-ATO-TRACE-ID") return TraceId
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class OrgSlf4JMdcInterceptor extends AbstractStaticInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Class<?> clazz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        if (Objects.isNull(clazz) || Objects.isNull(allArgs) || allArgs.length == 0 || Objects.isNull(allArgs[0])) {
            return result;
        }

        final String key = (String)allArgs[0];
        if (AutoTraceCtx.ATO_TRACE_ID.equalsIgnoreCase(key)) {
            return AutoTraceCtx.getTraceId();
        }
        if (AutoTraceCtx.ATO_SPAN_ID.equalsIgnoreCase(key)) {
            return AutoTraceCtx.getSpanId();
        }
        if (AutoTraceCtx.ATO_PARENT_SPAN_ID.equalsIgnoreCase(key)) {
            return AutoTraceCtx.getParentSpanId();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.slf4j.MDC");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isStatic().and(ElementMatchers.named("get")
                     .and(takesArgument(0, String.class)));
    }
}
