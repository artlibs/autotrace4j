package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractInstance;
import com.github.artlibs.autotrace4j.ctx.ReflectUtils;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * RocketMq Producer
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class RocketMqProducerInterceptor extends AbstractInstance {
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
        Map<String, String> properties = ReflectUtils.getFieldValue(allArgs[0], "properties");
        if (Objects.nonNull(properties)) {
            String spanId = properties.get(AutoTraceCtx.SPAN_KEY);
            String traceId = properties.get(AutoTraceCtx.TRACE_KEY);
            if (Objects.nonNull(traceId)) {
                AutoTraceCtx.setSpanId(spanId);
                AutoTraceCtx.setTraceId(traceId);
            } else {
                spanId = AutoTraceCtx.getSpanId();
                traceId = AutoTraceCtx.getTraceId();

                if (Objects.nonNull(traceId)) {
                    properties.put(AutoTraceCtx.SPAN_KEY, spanId);
                    properties.put(AutoTraceCtx.TRACE_KEY, traceId);
                }
            }
        }
    }

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl").or(ElementMatchers
                .named("com.aliyun.openservices.shade.com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl"));
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isPublic()
                // only rocket mq client have these three
                .and(ElementMatchers.named("send")
                        .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.apache.rocketmq.common.message.Message")))
                        .and(ElementMatchers.takesArgument(1, ElementMatchers.named("org.apache.rocketmq.client.producer.SendCallback")))
                        .and(ElementMatchers.takesArgument(2, long.class))
                ).or(
                        ElementMatchers.named("send")
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.apache.rocketmq.common.message.Message")))
                                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("org.apache.rocketmq.common.message.MessageQueue")))
                                .and(ElementMatchers.takesArgument(2, ElementMatchers.named("org.apache.rocketmq.client.producer.SendCallback")))
                                .and(ElementMatchers.takesArgument(3, long.class))
                ).or(
                        ElementMatchers.named("send")
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.apache.rocketmq.common.message.Message")))
                                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("org.apache.rocketmq.client.producer.MessageQueueSelector")))
                                .and(ElementMatchers.takesArgument(2, Object.class))
                                .and(ElementMatchers.takesArgument(3, ElementMatchers.named("org.apache.rocketmq.client.producer.SendCallback")))
                                .and(ElementMatchers.takesArgument(4, long.class))
                ).or(
                        ElementMatchers.named("sendKernelImpl")
                );
    }
}
