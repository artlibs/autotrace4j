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
 * Xxl Job Handler Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class XxlJobHandlerInterceptor extends AbstractInstanceInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) {
        AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
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
        return ElementMatchers.hasSuperClass(ElementMatchers
                .named("com.xxl.job.core.handler.IJobHandler"))
                .and(Transformer.getInterceptScopeJunction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("execute").and(ElementMatchers
                .takesArgument(0, String.class));
    }
}
