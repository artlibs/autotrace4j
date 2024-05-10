package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * RocketMq Producer Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class RocketMqProducerInterceptor extends AbstractInstanceInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isPublic()
                // only rocket mq client have these three
                .and(named("send")
                        .and(takesArgument(0, named("org.apache.rocketmq.common.message.Message")))
                        .and(takesArgument(1, named("org.apache.rocketmq.client.producer.SendCallback")))
                        .and(takesArgument(2, long.class))
                ).or(named("send")
                                .and(takesArgument(0, named("org.apache.rocketmq.common.message.Message")))
                                .and(takesArgument(1, named("org.apache.rocketmq.common.message.MessageQueue")))
                                .and(takesArgument(2, named("org.apache.rocketmq.client.producer.SendCallback")))
                                .and(takesArgument(3, long.class))
                ).or(named("send")
                                .and(takesArgument(0, named("org.apache.rocketmq.common.message.Message")))
                                .and(takesArgument(1, named("org.apache.rocketmq.client.producer.MessageQueueSelector")))
                                .and(takesArgument(2, Object.class))
                                .and(takesArgument(3, named("org.apache.rocketmq.client.producer.SendCallback")))
                                .and(takesArgument(4, long.class))
                ).or(named("sendKernelImpl"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        Map<String, String> properties = ReflectUtils.getFieldValue(allArgs[0], "properties", true);
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

}
