package io.github.artlibs.autotrace4j.transformer.abs;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.At4jTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * ASM Visitor
 *
 * @author Fury
 * @author suopovate
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbsVisitorTransformer implements At4jTransformer {

    // 一些JDK类在JVM启动的早期就已经被加载，此时类转换器的注入
    // 已经滞后，对于这些类的增强使用ASM Visitor模式来进行增强
    @Override
    public final DynamicType.Builder<?> transform(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader,
            JavaModule module,
            ProtectionDomain protectionDomain
    ) {
        DynamicType.Builder<?> newBuilder = builder;
        for (Map.Entry<Class<?>, ElementMatcher<? super MethodDescription>> entry
                : this.methodMatchers().entrySet()) {
            newBuilder = newBuilder.visit(Advice.to(entry.getKey()).on(entry.getValue()));
        }
        return newBuilder;
    }

    /**
     * build matcher map
     * @param matcher -
     * @return -
     */
    protected final Map<Class<?>, ElementMatcher<? super MethodDescription>> ofMatcher(
            ElementMatcher<? super MethodDescription> matcher) {
        return ofMatcher(this.getClass(), matcher);
    }

    /**
     * build matcher map
     * @param adviceLocationClass -
     * @param matcher -
     * @return -
     */
    protected final Map<Class<?>, ElementMatcher<? super MethodDescription>> ofMatcher(
            Class<?> adviceLocationClass, ElementMatcher<? super MethodDescription> matcher) {
        return newMmHolder().put(adviceLocationClass, matcher).get();
    }

    /**
     * new method matcher holder
     * @return -
     */
    protected final MethodMatcherHolder newMmHolder() {
        return new MethodMatcherHolder();
    }

    /**
     * build matchers map
     * @return -
     */
    protected abstract Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers();

    /**
     * MethodMatcherHolder
     */
    protected static class MethodMatcherHolder {
        /** just a holder map */
        private final Map<Class<?>, ElementMatcher<? super MethodDescription>> matcherMap = new HashMap<>(8);

        /**
         * put visitor class with method matcher
         * @param adviceLocationClass -
         * @param matcher -
         * @return -
         */
        public final MethodMatcherHolder put(
                Class<?> adviceLocationClass, ElementMatcher<? super MethodDescription> matcher) {
            matcherMap.put(adviceLocationClass, matcher);
            return this;
        }

        /**
         * get the map
         * @return -
         */
        public Map<Class<?>, ElementMatcher<? super MethodDescription>> get() {
            return matcherMap;
        }
    }

    public abstract static class Task extends AbsVisitorTransformer {
        protected abstract ElementMatcher<? super MethodDescription> methodMatcher();

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
            return ofMatcher(Task.class, methodMatcher());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DynamicType.Builder<?> transformType(
                DynamicType.Builder<?> builder,
                TypeDescription typeDescription,
                JavaModule module,
                ClassLoader classLoader
        ) {
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
                    .intercept(Advice.to(ConstructorVisitor.class));
        }

        private static class ConstructorVisitor {
            private ConstructorVisitor(){}

            @Advice.OnMethodExit
            public static void adviceOnMethodExit(
                    @Advice.FieldValue(value = TraceContext.TRACE_KEY, readOnly = false) String traceId,
                    @Advice.FieldValue(value = TraceContext.SPAN_KEY, readOnly = false) String spanId,
                    @Advice.FieldValue(value = TraceContext.PARENT_SPAN_KEY, readOnly = false) String parentSpanId
            ) {
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

        /**
         * advice on method enter
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
                @Advice.FieldValue(value = TraceContext.TRACE_KEY, readOnly = false) String traceId,
                @Advice.FieldValue(value = TraceContext.SPAN_KEY, readOnly = false) String spanId
        ) {
            try {
                // setup defined field on method exit
                TraceContext.setTraceId(traceId);
                TraceContext.setParentSpanId(spanId);
                TraceContext.setSpanId(TraceContext.generate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * advice on method exit: remove trace id
         */
        @Advice.OnMethodExit
        public static void adviceOnMethodExit() {
            TraceContext.removeAll();
        }
    }
}
