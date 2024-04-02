package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.interceptor.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Ons Mq Send Callback
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class OnsMqSendCallbackInterceptor extends RocketMqSendCallbackInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers
                .named("com.aliyun.openservices.ons.api.SendCallback"))
                .and(Transformer.getInterceptScopeJunction());
    }
}
