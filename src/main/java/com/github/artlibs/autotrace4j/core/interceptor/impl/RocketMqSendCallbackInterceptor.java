package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.InterceptorBuilder;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractCallbackInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * RocketMq Send Callback
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class RocketMqSendCallbackInterceptor extends AbstractCallbackInterceptor {
    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers
                .named("org.apache.rocketmq.client.producer.SendCallback"))
                .and(InterceptorBuilder.getInterceptScopeJunction());
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("onSuccess")
                .or(ElementMatchers.named("onException"));
    }
}
