package io.github.artlibs.autotrace4j.transformer;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;


/**
 * AutoTrace Transformer
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public interface At4jTransformer extends AgentBuilder.Transformer {
    /**
     * 类型匹配器，用来筛选需要转换增强的类
     * <p>
     * @return Type Matcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * 类型转换，可在此处为需要转换增强的类增加属性字段
     * <p>
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param javaModule JavaModule
     * @return 一个新的 DynamicType.Builder 对象
     */
    default DynamicType.Builder<?> transformType(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            JavaModule javaModule,
            ClassLoader classLoader) {
        return builder;
    }

    /**
     * 类型转换，可在此处为需要转换增强的类增加属性字段
     * <p>
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param javaModule JavaModule
     * @return 一个新的 DynamicType.Builder 对象
     */
    default DynamicType.Builder<?> transformTypeWithTrace(
            DynamicType.Builder<?> builder,
            @SuppressWarnings("unused") TypeDescription typeDescription,
            @SuppressWarnings("unused") JavaModule javaModule,
            @SuppressWarnings("unused") ClassLoader classLoader) {
        return builder
                // add field
                .defineField(TraceContext.TRACE_KEY, String.class, Visibility.PRIVATE)
                .defineField(TraceContext.SPAN_KEY, String.class, Visibility.PRIVATE)
                .defineField(TraceContext.PARENT_SPAN_KEY, String.class, Visibility.PRIVATE)
                // add getter
                .defineMethod(TraceContext.TRACE_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(TraceContext.TRACE_KEY))
                .defineMethod(TraceContext.SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(TraceContext.SPAN_KEY))
                .defineMethod(TraceContext.PARENT_SPAN_KEY_GETTER, String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(TraceContext.PARENT_SPAN_KEY))
                // add setter
                .defineMethod(TraceContext.TRACE_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(TraceContext.TRACE_KEY))
                .defineMethod(TraceContext.SPAN_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(TraceContext.SPAN_KEY))
                .defineMethod(TraceContext.PARENT_SPAN_KEY_SETTER, void.class, Visibility.PUBLIC)
                .withParameters(String.class)
                .intercept(FieldAccessor.ofField(TraceContext.PARENT_SPAN_KEY))
                // intercept constructor, any constructor
                .constructor(ElementMatchers.any())
                .intercept(Advice.to(AbsConstructorAdvice.class));
    }

    abstract class AbsConstructorAdvice {
        private AbsConstructorAdvice(){}

        @Advice.OnMethodExit
        @SuppressWarnings("unused")
        public static void adviceOnMethodExit(
                @Advice.FieldValue(value = TraceContext.TRACE_KEY, readOnly = false) String traceId,
                @Advice.FieldValue(value = TraceContext.SPAN_KEY, readOnly = false) String spanId,
                @Advice.FieldValue(value = TraceContext.PARENT_SPAN_KEY, readOnly = false) String parentSpanId) {
            try {
                // setup defined field on method exit
                traceId = TraceContext.getTraceId();
                spanId = TraceContext.getSpanId();
                parentSpanId = TraceContext.getParentSpanId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
