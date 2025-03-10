package io.github.artlibs.autotrace4j.transformer.abs;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.At4jTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.utility.nullability.MaybeNull;
import net.bytebuddy.utility.nullability.NeverNull;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ASM访问器模式转换器，用来增强JVM启动时就已经加载的JDK类
 * <p>
 * @author Fury
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public abstract class AbsVisitorTransformer implements At4jTransformer {

    /**
     * 一些JDK类在JVM启动的早期就已经被加载，此时类转换器的注入
     * 已经滞后，对于这些类的增强使用ASM Visitor模式来进行增强
     * {@inheritDoc}
     */
    @Override
    @NeverNull
    public final DynamicType.Builder<?> transform(
            @NeverNull DynamicType.Builder<?> builder,
            @NeverNull TypeDescription typeDescription,
            @MaybeNull ClassLoader classLoader,
            @MaybeNull JavaModule javaModule,
            @MaybeNull ProtectionDomain protectionDomain
    ) {
        DynamicType.Builder<?> newBuilder = transformType(builder, typeDescription, javaModule, classLoader);
        for (Map.Entry<Class<?>, ElementMatcher<? super MethodDescription>> entry
                : this.methodMatchers().get().entrySet()) {
            newBuilder = newBuilder.visit(Advice.to(entry.getKey()).on(entry.getValue()));
        }

        return newBuilder;
    }

    /**
     * 构建类-方法匹配器
     * <p>
     * @param methodMatcher 方法匹配器
     * @return 类-方法匹配器Holder
     */
    protected final MethodMatcherHolder ofMatcher(ElementMatcher<? super MethodDescription> methodMatcher) {
        return ofMatcher(this.getClass(), methodMatcher);
    }

    /**
     * 构建类-方法匹配器
     * <p>
     * @param adviceLocatedClass 增强类
     * @param methodMatcher 方法匹配器
     * @return 类-方法匹配器Holder
     */
    protected final MethodMatcherHolder ofMatcher(Class<?> adviceLocatedClass, ElementMatcher<? super MethodDescription> methodMatcher) {
        return new MethodMatcherHolder().ofMatcher(adviceLocatedClass, methodMatcher);
    }

    /**
     * 构建类-方法匹配器
     * <p>
     * @return 类-方法匹配器Holder
     */
    protected final MethodMatcherHolder ofNone() {
        return new MethodMatcherHolder();
    }

    /**
     * 获取类-方法匹配器Map
     * <p>
     * @return 类-方法匹配器Holder
     */
    protected abstract MethodMatcherHolder methodMatchers();

    /**
     * ASM Visitor类与方法匹配器持有者
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
        public final MethodMatcherHolder ofMatcher(
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

    /**
     * 为typeMatch匹配的对象注入Trace ID属性,记录当前上下文的Trace信息
     * 以便于当该属性被传递到另外一个上下文时可以获取到原上下文的Trace信息
     */
    public abstract static class AbsConstructor extends AbsVisitorTransformer {
        /**
         * {@inheritDoc}
         */
        @Override
        public DynamicType.Builder<?> transformType(
                DynamicType.Builder<?> builder,
                TypeDescription typeDescription,
                JavaModule module,
                ClassLoader classLoader) {
            return transformTypeWithTrace(builder, typeDescription, module, classLoader);
        }

        @Override
        protected MethodMatcherHolder methodMatchers() {
            return ofNone();
        }
    }

    /**
     * 适用于Thread任务对象及其执行方法
     * 1.增强该typeMatcher匹配的对象，注入属性
     * 2.在匹配的方法进入时从对象属性取Trace ID设置到上下文
     * 3.在匹配的方法结束时清空上下文的Trace信息
     */
    public abstract static class AbsTask extends AbsConstructor {
        protected abstract ElementMatcher<? super MethodDescription> methodMatcher();

        /**
         * {@inheritDoc}
         */
        @Override
        protected MethodMatcherHolder methodMatchers() {
            return ofMatcher(AbsTask.class, methodMatcher());
        }

        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
                @Advice.FieldValue(value = TraceContext.TRACE_KEY, readOnly = false) String traceId,
                @Advice.FieldValue(value = TraceContext.SPAN_KEY, readOnly = false) String spanId) {
            if (Objects.nonNull(traceId)) {
                TraceContext.setTraceId(traceId);
                TraceContext.setParentSpanId(spanId);
                TraceContext.setSpanId(TraceContext.generate());
            }
        }

        @Advice.OnMethodExit
        @SuppressWarnings("unused")
        public static void adviceOnMethodExit() {
            TraceContext.removeAll();
        }
    }
}
