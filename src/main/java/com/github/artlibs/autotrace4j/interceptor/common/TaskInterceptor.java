package com.github.artlibs.autotrace4j.interceptor.common;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.interceptor.Interceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * 功能：AbstractCallbackInstanceInterceptor
 *
 * @author Fury
 * @author suopovate
 * @since 2024/04/13
 * <p>
 * All rights Reserved.
 */
public interface TaskInterceptor extends Interceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    default DynamicType.Builder<?> doTypeTransform(
        DynamicType.Builder<?> builder,
        TypeDescription typeDescription,
        JavaModule module,
        ClassLoader classLoader
    ) {
        return builder  // add field
            .defineField(AutoTraceCtx.TRACE_KEY, String.class, Visibility.PRIVATE)
            .defineField(AutoTraceCtx.SPAN_KEY, String.class, Visibility.PRIVATE)
            // add getter
            .defineMethod(AutoTraceCtx.TRACE_KEY_GETTER, String.class, Visibility.PUBLIC)
            .intercept(FieldAccessor.ofField(AutoTraceCtx.TRACE_KEY))
            .defineMethod(AutoTraceCtx.SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
            .intercept(FieldAccessor.ofField(AutoTraceCtx.SPAN_KEY))
            // add setter
            .defineMethod(AutoTraceCtx.TRACE_KEY_SETTER, void.class, Visibility.PUBLIC)
            .withParameters(String.class)
            .intercept(FieldAccessor.ofField(AutoTraceCtx.TRACE_KEY))
            .defineMethod(AutoTraceCtx.SPAN_KEY_SETTER, void.class, Visibility.PUBLIC)
            .withParameters(String.class)
            .intercept(FieldAccessor.ofField(AutoTraceCtx.SPAN_KEY))
            // intercept constructor, any constructor
            .constructor(ElementMatchers.any())
            .intercept(Advice.to(ConstructorVisitor.class));
    }

    class ConstructorVisitor {
        @Advice.OnMethodExit
        public static void adviceOnMethodExit(
            @Advice.FieldValue(value = AutoTraceCtx.TRACE_KEY, readOnly = false) String traceId,
            @Advice.FieldValue(value = AutoTraceCtx.SPAN_KEY, readOnly = false) String spanId
        ) {
            try {
                // setup defined field on method exit
                traceId = AutoTraceCtx.getTraceId();
                spanId = AutoTraceCtx.getSpanId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
