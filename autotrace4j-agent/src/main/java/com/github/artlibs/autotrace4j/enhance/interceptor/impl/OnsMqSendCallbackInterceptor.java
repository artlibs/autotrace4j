package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.enhance.TraceBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 功能：增强 RocketMQ商业版 异步发送任务时的回调
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
                .and(TraceBuilder.getPackagePrefixesJunction());
    }
}
