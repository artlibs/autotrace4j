package com.github.artlibs.autotrace4j.interceptor.impl.jdk;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.jdk.ScheduledTask;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;
import java.util.concurrent.RunnableScheduledFuture;

import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * ScheduledThreadPool Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class ScheduledThreadPoolInterceptor extends AbstractVisitorInterceptor {

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(
            Advice
                .to(DelayedExecuteAdvisor.class)
                .on(isPrivate()
                        .and(named("delayedExecute"))
                        .and(ElementMatchers.takesArgument(0, named("java.util.concurrent.RunnableScheduledFuture")))
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    public static class DelayedExecuteAdvisor {

        /**
         * advice on method enter
         *
         * @param task the raw runnable task
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, readOnly = false
                , typing = Assigner.Typing.DYNAMIC) RunnableScheduledFuture<?> task
        ) {
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

}
