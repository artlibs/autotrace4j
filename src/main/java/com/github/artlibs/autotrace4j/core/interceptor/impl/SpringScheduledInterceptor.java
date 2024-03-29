package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.core.TraceAgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Method;

/**
 * Spring Task @Scheduled
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class SpringScheduledInterceptor extends AbstractInstanceInterceptor {
    /**
     * 在原方法刚开始进入时执行
     *
     * @param thiz         增强的对象实例
     * @param allArgs      原方法的参数表
     * @param originMethod 原方法
     * @throws Exception -
     */
    @Override
    public void beforeMethod(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
        AutoTraceCtx.setTraceId(AutoTraceCtx.generate());
    }

    /**
     * 在原方法返回前执行
     * @param thiz 增强的对象实例
     * @param allArgs 原方法的参数表
     * @param result 方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    @Override
    public Object afterMethod(Object thiz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        AutoTraceCtx.removeAll();
        return result;
    }

    /**
     * type matcher
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return TraceAgentBuilder.getInterceptScopeJunction()
            .and(ElementMatchers.not(ElementMatchers.isAnnotation()))
            .and(ElementMatchers.not(ElementMatchers.isInterface()))
            .and(ElementMatchers.not(ElementMatchers.nameContains("$")))
            .and(ElementMatchers.not(TypeDescription::isNestedClass))
            .and(ElementMatchers.declaresMethod(methodMatcher()));
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isAnnotatedWith(ElementMatchers
                .named("org.springframework.scheduling.annotation.Scheduled"));
    }
}
