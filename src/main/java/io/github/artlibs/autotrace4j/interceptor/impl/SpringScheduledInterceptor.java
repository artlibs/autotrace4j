package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.interceptor.Transformer;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Spring Task @Scheduled Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class SpringScheduledInterceptor extends AbstractInstanceInterceptor {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object obj, Object[] allArgs, Method originMethod) throws Exception {
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
        AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
        // There will be no parent span as this is a startup context
        AutoTraceCtx.setParentSpanId(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        AutoTraceCtx.removeAll();
        return result;
    }

}
