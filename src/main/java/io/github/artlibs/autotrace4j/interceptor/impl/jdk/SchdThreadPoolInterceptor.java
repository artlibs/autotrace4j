package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.ScheduledTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RunnableScheduledFuture;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ScheduledThreadPool Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class SchdThreadPoolInterceptor extends AbstractVisitorInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    @Override
    public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
        return ofMatcher(isPrivate()
                .and(named("delayedExecute"))
                .and(takesArgument(0, named("java.util.concurrent.RunnableScheduledFuture")))
        );
    }

    /**
     * advice on method enter
     *
     * @param task the raw runnable task
     */
    @Advice.OnMethodEnter
    private static void adviceOnMethodEnter(
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
