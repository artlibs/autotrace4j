package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
import com.github.artlibs.autotrace4j.enhance.AutoTrace;
import com.github.artlibs.autotrace4j.enhance.TraceBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Method;

/**
 * 功能：增强 Spring Task 的 @Scheduled 定时任务
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class ScheduledInterceptor extends Instance {
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
     * 类型匹配器, 这里要使用一个注解来辅助，避免把其他不需要的类也给代理了
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return TraceBuilder.getPackagePrefixesJunction()
            .and(ElementMatchers.not(ElementMatchers.isAnnotation()))
            .and(ElementMatchers.not(ElementMatchers.isInterface()))
            .and(ElementMatchers.not(ElementMatchers.nameContains("$")))
            .and(ElementMatchers.not(TypeDescription::isNestedClass))
            .and(ElementMatchers.isAnnotatedWith(AutoTrace.class));
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
