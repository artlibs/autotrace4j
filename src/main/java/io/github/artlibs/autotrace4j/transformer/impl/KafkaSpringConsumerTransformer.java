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
 * <code>org.springframework.kafka.listener.KafkaMessageListenerContainer:doStart()</code>
 * <code>org.springframework.kafka.listener.KafkaMessageListenerContainer.ListenerConsumer:run(),invokeListener(), invokeBatchOnMessageWithRecordsOrList() &amp; doInvokeOnMessage()</code>
 * <code>org.apache.kafka.clients.consumer.Consumer:poll()</code>
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class KafkaSpringConsumerTransformer extends AbsVisitorTransformer {

    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("org.springframework.kafka.listener." +
                "KafkaMessageListenerContainer$ListenerConsumer"));
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
     * @param recOrRecs ConsumerRecord&lt;K, V&gt; or ConsumerRecords&lt;K, V&gt;
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
        MethodWrapper lastHeader = ReflectUtils.getMethodWrapper(headers
                , "lastHeader", String.class);

        Object traceIdHeader = lastHeader.invoke(TraceContext.TRACE_KEY);
        byte[] traceIdByte = ReflectUtils.getMethodWrapper(traceIdHeader
                        , "value").invoke();
        Object spanIdHeader = lastHeader.invoke(TraceContext.SPAN_KEY);
        byte[] spanIdByte = ReflectUtils.getMethodWrapper(spanIdHeader
                        , "value").invoke();

        if (Objects.isNull(traceIdByte)) {
            traceIdByte = TraceContext.generate().getBytes();
        }
        TraceContext.setTraceId(new String(traceIdByte));
        TraceContext.setSpanId(TraceContext.generate());
        if (Objects.nonNull(spanIdByte)) {
            TraceContext.setParentSpanId(new String(spanIdByte));
        }
    }

    /**
     * 方法结束时清空上下文
     */
    @Advice.OnMethodExit
    @SuppressWarnings("unused")
    public static void adviceOnMethodExit() {
        TraceContext.removeAll();
    }
}
