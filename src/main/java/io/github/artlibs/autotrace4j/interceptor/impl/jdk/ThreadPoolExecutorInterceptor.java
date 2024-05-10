package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.PriorityTask;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * ThreadPoolExecutor Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ThreadPoolExecutorInterceptor extends AbstractVisitorInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.util.concurrent.ThreadPoolExecutor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
        return ofMatcher(named("execute").and(takesArgument(0, Runnable.class)));
    }

    /**
     * advice
     */
    @Advice.OnMethodEnter
    private static void adviceOnMethodEnter(@Advice.Argument(value = 0, readOnly = false
        , typing = Assigner.Typing.DYNAMIC) Runnable task) {
        try {
            if (Objects.nonNull(task)) {
                String traceId = AutoTraceCtx.getTraceId();
                if (Objects.nonNull(traceId) && !(task instanceof ThreadPoolTask)) {
                    if (task instanceof Comparable) {
                        task = new PriorityTask(task, traceId, AutoTraceCtx.getSpanId());
                    } else {
                        task = new ThreadPoolTask(task, traceId, AutoTraceCtx.getSpanId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
