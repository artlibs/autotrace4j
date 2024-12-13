package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

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
@SuppressWarnings("unused")
public class LogbackLog4jAsyncTransformer extends AbsVisitorTransformer {
    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return // logback - appendLoopOnAppenders
                named("ch.qos.logback.core.spi.AppenderAttachableImpl")
                        // log4j - appendLoopOnAppenders
                .or(named("org.apache.log4j.helpers.AppenderAttachableImpl"))
                        // log4j2 -
                .or(hasSuperType(named("org.apache.logging.log4j.core.config.AppenderControl")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPublic()
                // logback & log4j
                .and(named("appendLoopOnAppenders").and(returns(int.class)))
                // log4j2
                .or(named("callAppender").and(returns(void.class))));
    }

    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Object logEvent) {
        String traceId = ReflectUtils.getMethod(logEvent, TraceContext.TRACE_KEY_GETTER).invoke();
        String spanId = ReflectUtils.getMethod(logEvent, TraceContext.SPAN_KEY_GETTER).invoke();
        String parentSpanId = ReflectUtils.getMethod(logEvent, TraceContext.PARENT_SPAN_KEY_GETTER).invoke();

        // 异步的情况下：上下文没有traceId或者traceId对不上
        String ctxTraceId = TraceContext.getTraceId();
        String ctxSpanId = TraceContext.getSpanId();
        String ctxParentSpanId = TraceContext.getParentSpanId();
        boolean isSyncLogger = Objects.equals(ctxTraceId, traceId) &&
                Objects.equals(ctxSpanId, spanId) &&
                Objects.equals(ctxParentSpanId, parentSpanId);

        // Worker只有一个线程，上一次设置之后并未清空，需要通过重复覆盖设置才能覆盖上一次的值
        if (Objects.nonNull(traceId) && !isSyncLogger) {
            TraceContext.setTraceId(traceId);
            TraceContext.setSpanId(spanId);
            TraceContext.setParentSpanId(parentSpanId);
        }
    }
}
