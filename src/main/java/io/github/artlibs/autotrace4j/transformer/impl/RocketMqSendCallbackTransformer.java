package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isAbstract;

/**
 * RocketMq Send Callback增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class RocketMqSendCallbackTransformer extends AbsDelegateTransformer.AbsAnonymousInterface {
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return not(isAbstract()).and(not(isInterface())).and(
                hasSuperType(named("org.apache.rocketmq.client.producer.SendCallback")
                        .or(named("com.aliyun.openservices.ons.api.SendCallback")))
        );

    }

    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("onSuccess").or(named("onException"));
    }
}
