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
 * RocketMq Listener Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class RocketMqListenerTransformer extends AbsDelegateTransformer.Instance {
    private static final String GUP = "getUserProperty";


    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("doConvertMessage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
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
