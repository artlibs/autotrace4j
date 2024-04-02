package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.InterceptorBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Ons Mq Send Callback
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class OnsMqSendCallbackInterceptor extends RocketMqSendCallbackInterceptor {
    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers
                .named("com.aliyun.openservices.ons.api.SendCallback"))
                .and(InterceptorBuilder.getInterceptScopeJunction());
    }
}
