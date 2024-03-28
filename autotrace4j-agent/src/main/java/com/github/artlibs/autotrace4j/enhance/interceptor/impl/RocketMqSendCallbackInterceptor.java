package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.enhance.TraceBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 功能：增强 RocketMQ 异步发送任务时的回调
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class RocketMqSendCallbackInterceptor extends CallbackInterfaceInterceptor {
    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers
                .named("org.apache.rocketmq.client.producer.SendCallback"))
                .and(TraceBuilder.getPackagePrefixesJunction());
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
