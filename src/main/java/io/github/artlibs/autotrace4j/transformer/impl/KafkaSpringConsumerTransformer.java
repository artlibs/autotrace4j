package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Iterator;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Spring Kafka 消费增强转换器, 参见类
 * org.springframework.kafka.listener.KafkaMessageListenerContainer:doStart()
 * org.springframework.kafka.listener.KafkaMessageListenerContainer
 *  .ListenerConsumer:run(),invokeListener(), invokeBatchOnMessageWithRecordsOrList() & doInvokeOnMessage()
 * org.apache.kafka.clients.consumer.Consumer:poll()
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class KafkaSpringConsumerTransformer extends AbsVisitorTransformer {

    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("org.springframework.kafka.listener." +
                "KafkaMessageListenerContainer.ListenerConsumer"));
    }

    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPrivate().and(
                // private void invokeBatchOnMessageWithRecordsOrList(final ConsumerRecords<K, V> recordsArg,
                //				@Nullable List<ConsumerRecord<K, V>> recordListArg)
                named("invokeBatchOnMessageWithRecordsOrList")
                        // private void doInvokeOnMessage(final ConsumerRecord<K, V> recordArg)
                        .or(named("doInvokeOnMessage"))
        ));
    }

    /**
     * 消费消息时将消息头的trace信息放入上下文, 或者生成新的上下文
     * @param recOrRecs ConsumerRecord<K, V> or ConsumerRecords<K, V>
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(@Advice.Argument(value = 0) Object recOrRecs) {
        Object consumerRecord = recOrRecs;
        if (Iterable.class.isAssignableFrom(recOrRecs.getClass())) {
            Iterator<?> iterator = ReflectUtils.getMethodWrapper(recOrRecs
                    , "iterator").invoke();
            consumerRecord = ReflectUtils.getMethodWrapper(iterator
                    , "next").invoke();
        }
        Object headers = ReflectUtils.getMethodWrapper(consumerRecord
                , "headers").invoke();
        MethodWrapper method = ReflectUtils.getMethodWrapper(headers
                , "lastHeader", String.class);

        String traceId = method.invoke(TraceContext.TRACE_KEY);
        if (Objects.isNull(traceId)) {
            traceId = TraceContext.generate();
        }
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(TraceContext.generate());
        String spanId = method.invoke(TraceContext.SPAN_KEY);
        if (Objects.nonNull(spanId)) {
            TraceContext.setParentSpanId(spanId);
        }
    }

    /**
     * 方法结束时清空上下文
     */
    @Advice.OnMethodExit
    public static void adviceOnMethodExit() {
        TraceContext.removeAll();
    }
}
