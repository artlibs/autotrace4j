package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Kafka 生产者增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class KafkaProducerTransformer extends AbsVisitorTransformer {

    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("org.apache.kafka.clients.producer.KafkaProducer"));
    }

    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPrivate().and(named("doSend"))
                .and(takesArgument(0, hasSuperType(named("org.apache.kafka.clients.producer.ProducerRecord"))))
                .and(takesArgument(1, hasSuperType(named("org.apache.kafka.clients.producer.Callback"))))
        );
    }

    /**
     * 发送消息时将当前上下文trace信息放入消息头
     * @param producerRecord -
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Object producerRecord) {
        final String traceId = TraceContext.getTraceId();
        if (Objects.isNull(producerRecord) || Objects.isNull(traceId)) {
            return;
        }
        Object headers = ReflectUtils.getMethodWrapper(producerRecord
                        , "headers").invoke();
        if (Objects.isNull(headers)) {
            return;
        }
        MethodWrapper method = ReflectUtils.getMethodWrapper(headers
                , "add", String.class, byte[].class);

        method.invoke(TraceContext.TRACE_KEY, traceId.getBytes());
        if (Objects.nonNull(TraceContext.getSpanId())) {
            method.invoke(TraceContext.SPAN_KEY, TraceContext.getSpanId().getBytes());
        }
    }
}
