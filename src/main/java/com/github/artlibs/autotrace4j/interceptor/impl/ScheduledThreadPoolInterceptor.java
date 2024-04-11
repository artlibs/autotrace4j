package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.jdk.ScheduledTask;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * ScheduledThreadPool Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ScheduledThreadPoolInterceptor extends AbstractVisitorInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.is(java.util.concurrent.ScheduledThreadPoolExecutor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isPrivate().and(ElementMatchers.named("delayedExecute"))
                .and(ElementMatchers.takesArgument(0, RunnableScheduledFuture.class));
    }

    /**
     * advice on method enter
     * @param task the raw runnable task
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(@Advice.Argument(value = 0, readOnly = false
            , typing = Assigner.Typing.DYNAMIC) RunnableScheduledFuture<?> task) {
        try {
            if (Objects.nonNull(task)) {
                String traceId = AutoTraceCtx.getTraceId();
                if (Objects.nonNull(traceId) && !(task instanceof ScheduledTask)) {
                    task = new ScheduledTask<>(task, traceId, AutoTraceCtx.getSpanId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
