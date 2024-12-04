package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Logback/Log4j - 增强转换器
 *      异步Log进入appendLoopOnAppenders方法时从Event拿出ID设置到上下文
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class LogbackLog4jAsyncTransformer extends AbsDelegateTransformer.AbsInstance {
    /**
     * 只往类注入属性
     * <p>
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("ch.qos.logback.core.spi.AppenderAttachableImpl")
                .or(named("org.apache.log4j.helpers.AppenderAttachableImpl"));
    }

    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return isPublic().and(named("appendLoopOnAppenders"))
                .and(returns(int.class));
    }

    @Override
    protected void onMethodEnter(Object obj, Object[] allArgs, Method originMethod) throws Exception {
        if (Objects.isNull(allArgs) || allArgs.length < 1) {
            return;
        }

        Object logEventObj = allArgs[0];
        String traceId = ReflectUtils.getFieldValue(logEventObj, TraceContext.TRACE_KEY);
        if (Objects.nonNull(traceId)) {
            TraceContext.setTraceId(traceId);
            TraceContext.setSpanId(TraceContext.generate());
        }
        String spanId = ReflectUtils.getFieldValue(logEventObj, TraceContext.SPAN_KEY);
        if (Objects.nonNull(spanId)) {
            TraceContext.setParentSpanId(spanId);
        }
    }
}
