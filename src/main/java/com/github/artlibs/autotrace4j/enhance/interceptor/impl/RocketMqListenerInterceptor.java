package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
import com.github.artlibs.autotrace4j.support.ReflectUtils;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 功能：增强 Rocket MQ 消费
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class RocketMqListenerInterceptor extends Instance {
    private static final String GUP = "getUserProperty";

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
        ReflectUtils.WrappedMethod method = ReflectUtils.getMethod(allArgs[0], GUP, String.class);

        String traceId = method.invoke(AutoTraceCtx.TRACE_KEY);
        String parentSpanId = method.invoke(AutoTraceCtx.SPAN_KEY);

        if (Objects.isNull(traceId)) {
            // 如果MQ消费时没有trace id, 生成一个
            traceId = AutoTraceCtx.generate();
        }
        AutoTraceCtx.setTraceId(traceId);
        AutoTraceCtx.setParentSpanId(parentSpanId);
        AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
    }

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer");
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("doConvertMessage");
    }
}
