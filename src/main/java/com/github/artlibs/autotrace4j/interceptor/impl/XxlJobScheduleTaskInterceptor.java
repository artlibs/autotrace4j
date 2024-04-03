package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.interceptor.Transformer;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Xxl Job Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class XxlJobScheduleTaskInterceptor extends AbstractVisitorInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return Transformer.getInterceptScopeJunction()
        .and(hasSuperClass(named("com.xxl.job.core.handler.IJobHandler"))
                // Or has functions annotated with @XxlJob
                .or(declaresMethod(isAnnotatedWith(
                    named(("com.xxl.job.core.handler.annotation.XxlJob"))))
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return isAnnotatedWith(named("com.xxl.job.core.handler.annotation.XxlJob"))
                .or(named("execute").and(takesNoArguments()))
                .or(named("execute").and(takesArgument(0, String.class)));
    }

    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter() {
        AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
        // There will be no parent span as this is a startup context
        AutoTraceCtx.setParentSpanId(null);
    }

    @Advice.OnMethodExit
    public static void adviceOnMethodExit() {
        AutoTraceCtx.removeAll();
    }
}
