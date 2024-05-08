package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.PriorityTask;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;

/**
 * ThreadPoolExecutor Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ThreadPoolExecutorInterceptor extends AbstractVisitorInterceptor {

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(
            Advice
                .to(DoExecuteAdvisor.class)
                .on(ElementMatchers.named("execute").and(ElementMatchers.takesArgument(0, Runnable.class)))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("java.util.concurrent.ThreadPoolExecutor");
    }

    public static class DoExecuteAdvisor {

        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(@Advice.Argument(value = 0, readOnly = false
            , typing = Assigner.Typing.DYNAMIC) Runnable task) throws Exception {
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

}
