package com.github.artlibs.autotrace4j.interceptor.impl.jdk;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.jdk.WrapForkTask;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Objects;
import java.util.concurrent.ForkJoinTask;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ForkJoinPool Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class ForkJoinPoolInterceptor extends AbstractVisitorInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return is(java.util.concurrent.ForkJoinPool.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("externalSubmit").and(takesArgument(0
                , hasSuperClass(named("java.util.concurrent.ForkJoinTask"))));
    }

    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(@Advice.Argument(value = 0, readOnly = false
            , typing = Assigner.Typing.DYNAMIC) ForkJoinTask<?> task) throws Exception {
        try {
            if (Objects.nonNull(task)) {
                String traceId = AutoTraceCtx.getTraceId();
                if (Objects.nonNull(traceId)) {
                    task = new WrapForkTask<>(task, traceId, AutoTraceCtx.getSpanId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
