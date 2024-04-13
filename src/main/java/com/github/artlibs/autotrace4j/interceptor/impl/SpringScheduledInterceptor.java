package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.interceptor.Transformer;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Spring Task @Scheduled Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class SpringScheduledInterceptor extends AbstractVisitorInterceptor {

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(
            Advice
                .to(ScheduledAnnotatedAdvisor.class)
                .on(isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled")))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return Transformer.getInterceptScopeJunction()
            .and(not(isAnnotation()))
            .and(not(isInterface()))
            .and(not(nameContains("$")))
            .and(declaresMethod(isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled"))));
    }

    public static class ScheduledAnnotatedAdvisor {

        /**
         * advice on method enter: set trace id
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter() {
            AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
            AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
            // There will be no parent span as this is a startup context
            AutoTraceCtx.setParentSpanId(null);
        }

        /**
         * advice on method exit: remove trace id
         */
        @Advice.OnMethodExit
        public static void adviceOnMethodExit() {
            AutoTraceCtx.removeAll();
        }

    }

}
