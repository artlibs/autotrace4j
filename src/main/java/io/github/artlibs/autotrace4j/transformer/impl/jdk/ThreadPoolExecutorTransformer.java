package io.github.artlibs.autotrace4j.transformer.impl.jdk;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.jdk.PriorityTask;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
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
 * ThreadPoolExecutor增强转换器
 *      提交一个任务时如果当前上下文存在Trace ID则传递
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ThreadPoolExecutorTransformer extends AbsVisitorTransformer {

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
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(named("execute").and(takesArgument(0, Runnable.class)));
    }

    @Advice.OnMethodEnter
    private static void adviceOnMethodEnter(@Advice.Argument(value = 0, readOnly = false
        , typing = Assigner.Typing.DYNAMIC) Runnable task) {
        try {
            if (Objects.nonNull(task)) {
                String traceId = TraceContext.getTraceId();
                if (Objects.nonNull(traceId) && !(task instanceof ThreadPoolTask)) {
                    if (task instanceof Comparable) {
                        task = new PriorityTask(task, traceId, TraceContext.getSpanId());
                    } else {
                        task = new ThreadPoolTask(task, traceId, TraceContext.getSpanId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
