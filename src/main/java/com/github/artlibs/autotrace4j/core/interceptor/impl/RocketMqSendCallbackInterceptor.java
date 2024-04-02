package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.Transformer;
import com.github.artlibs.autotrace4j.core.interceptor.common.AbstractCallbackInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * RocketMq SendCallback Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class RocketMqSendCallbackInterceptor extends AbstractCallbackInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers
                .named("org.apache.rocketmq.client.producer.SendCallback"))
                .and(Transformer.getInterceptScopeJunction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("onSuccess").or(ElementMatchers.named("onException"));
    }
}
