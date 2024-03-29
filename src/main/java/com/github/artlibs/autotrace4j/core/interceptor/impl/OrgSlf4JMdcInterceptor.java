package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractStaticInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Method;
import java.util.Objects;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * 当通过 MDC.get("X-ATO-SPAN-ID")时返回当前上下文的 SpanId
 * 当通过 MDC.get("X-ATO-P-SPAN-ID")时返回当前上下文的 ParentSpanId
 * 当通过 MDC.get("X-ATO-TRACE-ID")时返回当前上下文的 TraceId
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class OrgSlf4JMdcInterceptor extends AbstractStaticInterceptor {

    /**
     * 在原方法返回前执行
     * @param clazz 增强的方法所在类
     * @param allArgs 原方法的参数表
     * @param result 方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    @Override
    public Object afterMethod(Class<?> clazz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        if (Objects.isNull(clazz) || Objects.isNull(allArgs) || allArgs.length == 0 || Objects.isNull(allArgs[0])) {
            return result;
        }

        // 支持通过 MDC.get("X-ATO-TRACE-ID") 获取当前上下文的 Trace Id
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
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.slf4j.MDC");
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isStatic().and(ElementMatchers.named("get")
                     .and(takesArgument(0, String.class)));
    }
}
