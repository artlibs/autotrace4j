package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.interceptor.Transformer;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Xxl Job Handler Interceptor
 *
 * @author Fury
 * @since 2024-03-30
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
        return Transformer.getInterceptScopeJunction()
                .and(hasSuperClass(named("com.xxl.job.core.handler.IJobHandler")))
                    // remove since xxl-job v2.2.0
//                .or(isAnnotatedWith(named("com.xxl.job.core.handler.annotation.JobHandler")))
//                .or(not(isAnnotation()).and(not(isInterface()))
//                        .and(not(nameContains("$")))
//                        .and(declaresMethod(isAnnotatedWith(
//                        named("com.xxl.job.core.handler.annotation.XxlJob")))))
                ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return isPublic()  // add since xxl-job v2.3.0
                .and(named("execute").and(takesNoArguments()).and(returns(void.class)))
                // remove since xxl-job v2.3.0
                .or(named("execute").and(takesArgument(0, String.class)))
                ;
    }
}
