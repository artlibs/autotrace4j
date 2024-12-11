package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * RocketMq Listener增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class RocketMqListenerTransformer extends AbsDelegateTransformer.AbsInstance {
    private static final String GUP = "getUserProperty";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer");
    }

    /**
     * 由于是在转换消息时设置的信息，所以不能在方法结束时清空trace上下文
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("doConvertMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[0], GUP, String.class);

        String traceId = methodWrapper.invoke(TraceContext.TRACE_KEY);
        String parentSpanId = methodWrapper.invoke(TraceContext.SPAN_KEY);

        if (Objects.isNull(traceId)) {
            traceId = TraceContext.generate();
        }
        TraceContext.setTraceId(traceId);
        TraceContext.setParentSpanId(parentSpanId);
        TraceContext.setSpanId(TraceContext.generate());
    }

}
