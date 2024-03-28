package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
import com.github.artlibs.autotrace4j.support.ReflectUtils;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 功能：增强异步回调上下文
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class CallbackInterfaceInterceptor extends Instance {
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
        String traceId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.TRACE_KEY);
        if (Objects.nonNull(traceId)) {
            AutoTraceCtx.setTraceId(traceId);
        }

        String spanId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.SPAN_KEY);
        if (Objects.nonNull(spanId)) {
            AutoTraceCtx.setSpanId(spanId);
        }

        String parentSpanId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.PARENT_SPAN_KEY);
        if (Objects.nonNull(parentSpanId)) {
            AutoTraceCtx.setParentSpanId(parentSpanId);
        }
    }

    /**
     * 类型转换，如增加字段、方法等
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @return new DynamicType.Builder
     */
    @Override
    public DynamicType.Builder<?> transformType(DynamicType.Builder<?> builder
            , TypeDescription typeDescription, ClassLoader classLoader) {
        return builder.defineField(AutoTraceCtx.TRACE_KEY, String.class, Visibility.PRIVATE)
            .defineField(AutoTraceCtx.SPAN_KEY, String.class, Visibility.PRIVATE)
            .defineField(AutoTraceCtx.PARENT_SPAN_KEY, String.class, Visibility.PRIVATE)

            .defineMethod(AutoTraceCtx.TRACE_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.TRACE_KEY))
            .defineMethod(AutoTraceCtx.SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.SPAN_KEY))
            .defineMethod(AutoTraceCtx.PARENT_SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.PARENT_SPAN_KEY))

            .defineMethod(AutoTraceCtx.TRACE_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.TRACE_KEY))
            .defineMethod(AutoTraceCtx.SPAN_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.SPAN_KEY))
            .defineMethod(AutoTraceCtx.PARENT_SPAN_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.PARENT_SPAN_KEY))

            // intercept constructor, any constructor
                .constructor(ElementMatchers.any())
                .intercept(Advice.to(CallbackConstructor.class));
    }

    public static class CallbackConstructor {
        private CallbackConstructor() {}

        @Advice.OnMethodExit
        public static void intercept(
            @Advice.FieldValue(value = AutoTraceCtx.TRACE_KEY, readOnly = false) String traceId,
            @Advice.FieldValue(value = AutoTraceCtx.SPAN_KEY, readOnly = false) String spanId,
            @Advice.FieldValue(value = AutoTraceCtx.PARENT_SPAN_KEY, readOnly = false) String parentSpanId
        ) {
            try {
                traceId = AutoTraceCtx.getTraceId();
                spanId = AutoTraceCtx.getSpanId();
                parentSpanId = AutoTraceCtx.getParentSpanId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
