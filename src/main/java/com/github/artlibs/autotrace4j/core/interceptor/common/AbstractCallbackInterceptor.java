package com.github.artlibs.autotrace4j.core.interceptor.common;

import com.github.artlibs.autotrace4j.core.interceptor.base.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.ctx.ReflectUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Callback Interface
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractCallbackInterceptor extends AbstractInstanceInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
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
     * {@inheritDoc}
     */
    @Override
    public DynamicType.Builder<?> doTypeTransform(DynamicType.Builder<?> builder
            , TypeDescription typeDescription, JavaModule module, ClassLoader classLoader) {
        return builder  // add field
            .defineField(AutoTraceCtx.TRACE_KEY, String.class, Visibility.PRIVATE)
            .defineField(AutoTraceCtx.SPAN_KEY, String.class, Visibility.PRIVATE)
            .defineField(AutoTraceCtx.PARENT_SPAN_KEY, String.class, Visibility.PRIVATE)
                        // add getter
            .defineMethod(AutoTraceCtx.TRACE_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.TRACE_KEY))
            .defineMethod(AutoTraceCtx.SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.SPAN_KEY))
            .defineMethod(AutoTraceCtx.PARENT_SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(AutoTraceCtx.PARENT_SPAN_KEY))
                        // add setter
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
                .intercept(Advice.to(this.getClass()));
    }

    @Advice.OnMethodExit
    public static void adviceOnMethodExit(
        @Advice.FieldValue(value = AutoTraceCtx.TRACE_KEY, readOnly = false) String traceId,
        @Advice.FieldValue(value = AutoTraceCtx.SPAN_KEY, readOnly = false) String spanId,
        @Advice.FieldValue(value = AutoTraceCtx.PARENT_SPAN_KEY, readOnly = false) String parentSpanId
    ) {
        try {
            // setup defined field on method exit
            traceId = AutoTraceCtx.getTraceId();
            spanId = AutoTraceCtx.getSpanId();
            parentSpanId = AutoTraceCtx.getParentSpanId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
