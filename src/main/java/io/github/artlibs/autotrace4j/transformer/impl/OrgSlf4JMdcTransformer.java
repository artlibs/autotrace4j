package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * 增强SLF4J的MDC,支持通过其获取到三个Trace ID
 * <p>
 * MDC.get("X-ATO-SPAN-ID") return SpanId
 * MDC.get("X-ATO-P-SPAN-ID") return ParentSpanId
 * MDC.get("X-ATO-TRACE-ID") return TraceId
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class OrgSlf4JMdcTransformer extends AbsDelegateTransformer.Static {

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
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isStatic().and(ElementMatchers.named("get")
                .and(takesArgument(0, String.class)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object onMethodExit(Class<?> clazz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        if (Objects.isNull(clazz) || Objects.isNull(allArgs) || allArgs.length == 0 || Objects.isNull(allArgs[0])) {
            return result;
        }

        final String key = (String)allArgs[0];
        if (TraceContext.ATO_TRACE_ID.equalsIgnoreCase(key)) {
            return TraceContext.getTraceId();
        }
        if (TraceContext.ATO_SPAN_ID.equalsIgnoreCase(key)) {
            return TraceContext.getSpanId();
        }
        if (TraceContext.ATO_PARENT_SPAN_ID.equalsIgnoreCase(key)) {
            return TraceContext.getParentSpanId();
        }

        return result;
    }

}
