package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.Transformer;
import com.github.artlibs.autotrace4j.core.interceptor.base.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

/**
 * Spring Task @Scheduled Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class SpringScheduledInterceptor extends AbstractInstanceInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) {
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
        AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) {
        AutoTraceCtx.removeAll();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return Transformer.getInterceptScopeJunction()
            .and(ElementMatchers.not(ElementMatchers.isAnnotation()))
            .and(ElementMatchers.not(ElementMatchers.isInterface()))
            .and(ElementMatchers.not(ElementMatchers.nameContains("$")))
            .and(ElementMatchers.not(TypeDescription::isNestedClass))
            .and(ElementMatchers.declaresMethod(methodMatcher()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isAnnotatedWith(ElementMatchers
                .named("org.springframework.scheduling.annotation.Scheduled"));
    }
}
