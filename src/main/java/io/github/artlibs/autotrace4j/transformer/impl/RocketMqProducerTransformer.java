package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * RocketMq Producer增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class RocketMqProducerTransformer extends AbsDelegateTransformer.AbsInstance {
    private static final String SEND_CB_TYPE = "org.apache.rocketmq.common.message.Message";
    private static final String MESSAGE_TYPE = "org.apache.rocketmq.client.producer.SendCallback";

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
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return isPublic()
                // only rocket mq client have these three
                .and(named("send")
                        .and(takesArgument(0, named(SEND_CB_TYPE)))
                        .and(takesArgument(1, named(MESSAGE_TYPE)))
                        .and(takesArgument(2, long.class))
                ).or(named("send")
                                .and(takesArgument(0, named(SEND_CB_TYPE)))
                                .and(takesArgument(1, named("org.apache.rocketmq.common.message.MessageQueue")))
                                .and(takesArgument(2, named(MESSAGE_TYPE)))
                                .and(takesArgument(3, long.class))
                ).or(named("send")
                                .and(takesArgument(0, named(SEND_CB_TYPE)))
                                .and(takesArgument(1, named("org.apache.rocketmq.client.producer.MessageQueueSelector")))
                                .and(takesArgument(2, Object.class))
                                .and(takesArgument(3, named(MESSAGE_TYPE)))
                                .and(takesArgument(4, long.class))
                ).or(named("sendKernelImpl"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        Map<String, String> properties = ReflectUtils.getFieldValue(allArgs[0], "properties", true);
        if (Objects.nonNull(properties)) {
            String spanId = properties.get(TraceContext.SPAN_KEY);
            String traceId = properties.get(TraceContext.TRACE_KEY);
            if (Objects.nonNull(traceId)) {
                TraceContext.setSpanId(spanId);
                TraceContext.setTraceId(traceId);
            } else {
                spanId = TraceContext.getSpanId();
                traceId = TraceContext.getTraceId();

                if (Objects.nonNull(traceId)) {
                    properties.put(TraceContext.SPAN_KEY, spanId);
                    properties.put(TraceContext.TRACE_KEY, traceId);
                }
            }
        }
    }

}
