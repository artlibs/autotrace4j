package io.github.artlibs.autotrace4j.transformer.abs;

import io.github.artlibs.autotrace4j.AutoTrace4j;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.support.Constants;
import io.github.artlibs.autotrace4j.transformer.At4jTransformer;
import io.github.artlibs.autotrace4j.transformer.MorphCallable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Objects;

import static io.github.artlibs.autotrace4j.support.Constants.INTERCEPT_METHOD_NAME;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;

/**
 * 走方法代理模式的转换器
 * <p>
 * @author Fury
 * @since 2024-03-30

 * @param <T> delegate type
 * All rights Reserved.
 */
public abstract class AbsDelegateTransformer<T> implements At4jTransformer {
    /**
     * {@inheritDoc}
     */
    @Override
    public final DynamicType.Builder<?> transform(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader,
            JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        // 使用方法代理方式进行增强时支持为目标类增加成员属性
        return transformType(builder, typeDescription, javaModule, classLoader)
                .method(isMethod().and(this.methodMatcher()))
                .intercept(MethodDelegation
                        .withDefaultConfiguration()
                        // 增强方法中用到了@Morph注解，该注解不在默认的寻找增强方法的绑定器列表中，需要手动添加
                        .withBinders(Morph.Binder.install(MorphCallable.class))
                        // 为寻找增强方法指定过滤器，这里使用指定名称来过滤：即寻找目标类下面名为该方法名的方法作为增强逻辑
                        .filter(target -> INTERCEPT_METHOD_NAME.equals(target.getActualName()) &&
                                target.getDeclaredAnnotations().isAnnotationPresent(RuntimeType.class))
                        .to(this)
                );
    }

    /**
     * 方法匹配器，在需要转换增强的类当中寻找匹配的方法
     * <p>
     * @return Method Matcher
     */
    protected abstract ElementMatcher<? super MethodDescription> methodMatcher();


    /**
     * 当进入目标方法时需要执行的动作
     * <p>
     * @param obj thiz or class
     * @param allArgs argument list
     * @param originMethod original method
     * @throws Exception -
     */
    protected void onMethodEnter(T obj, Object[] allArgs, Method originMethod) throws Exception {
        // NO Sonar
    }

    /**
     * 当退出目标方法时需要执行的动作
     * <p>
     * @param obj thiz or class
     * @param allArgs argument list
     * @param result method result
     * @param originMethod original method
     * @return Object - result
     * @throws Exception -
     */
    protected Object onMethodExit(T obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        return result;
    }

    protected ElementMatcher.Junction<TypeDescription> bizScopeJunction() {
        return AutoTrace4j.Transformer.getBizScopeJunction();
    }

    /**
     * do intercept
     * @param obj class or thiz
     * @param zuper morph super
     * @param args argument list
     * @param method original method
     * @return result
     */
    protected Object doIntercept(T obj, MorphCallable zuper, Object[] args, Method method) {
        try {
            this.onMethodEnter(obj, args, method);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object result = null;
        try {
            result = zuper.call(args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                result = this.onMethodExit(obj, args, result, method);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode() + methodMatcher().hashCode() + typeMatcher().hashCode();
    }

    public abstract static class Instance extends AbsDelegateTransformer<Object> {
        /**
         * transform instance method
         * <p>
         * @param thiz the object
         * @param zuper the original object
         * @param args argument list
         * @param originMethod original method
         * @return result
         */
        @RuntimeType
        public Object intercept(@This Object thiz, @Morph MorphCallable zuper
                , @AllArguments Object[] args, @Origin Method originMethod) {
            return this.doIntercept(thiz, zuper, args, originMethod);
        }
    }

    public abstract static class Static extends AbsDelegateTransformer<Class<?>> {
        /**
         * transform class method
         * <p>
         * @param clazz the class object
         * @param zuper the original object
         * @param args argument list
         * @param originMethod original method
         * @return result
         */
        @RuntimeType
        public Object intercept(@Origin Class<?> clazz, @Morph MorphCallable zuper
                , @AllArguments Object[] args, @Origin Method originMethod) {
            return this.doIntercept(clazz, zuper, args, originMethod);
        }
    }

    public abstract static class Servlet extends Instance {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
            Class<?> httpReqClazz;
            Class<?> httpRespClazz;
            try {
                httpReqClazz = Class.forName(
                        "javax.servlet.http.HttpServletRequest", false, Thread.currentThread().getContextClassLoader()
                );
                httpRespClazz = Class.forName(
                        "javax.servlet.http.HttpServletResponse", false, Thread.currentThread().getContextClassLoader()
                );
            } catch (ClassNotFoundException e) {
                // warning that we can't intercept the servlet
                return;
            }
            Class<?> argServletReqClazz = allArgs[0].getClass();
            Class<?> argServletRespClazz = allArgs[1].getClass();
            boolean isHttp = httpReqClazz.isAssignableFrom(argServletReqClazz) && httpRespClazz.isAssignableFrom(argServletRespClazz);
            // just intercept http request
            if (isHttp) {
                // first we take it from the req attributes
                String traceId = ReflectUtils
                        .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                        .invoke(TraceContext.ATO_TRACE_ID);
                String parentSpanId = ReflectUtils
                        .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                        .invoke(TraceContext.ATO_PARENT_SPAN_ID);
                String spanId = ReflectUtils
                        .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                        .invoke(TraceContext.ATO_SPAN_ID);

                if (Objects.nonNull(traceId)) {
                    TraceContext.setTraceId(traceId);
                    TraceContext.setParentSpanId(parentSpanId);
                    TraceContext.setSpanId(spanId);
                } else {
                    traceId = ReflectUtils
                            .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                            .invoke(TraceContext.ATO_TRACE_ID);
                    parentSpanId = ReflectUtils
                            .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                            .invoke(TraceContext.ATO_SPAN_ID);

                    if (Objects.isNull(traceId)) {
                        traceId = TraceContext.generate();
                    }
                    spanId = TraceContext.generate();

                    TraceContext.setSpanId(spanId);
                    TraceContext.setTraceId(traceId);
                    TraceContext.setParentSpanId(parentSpanId);

                    ReflectUtils
                            .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                            .invoke(TraceContext.ATO_TRACE_ID, traceId);
                    ReflectUtils
                            .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                            .invoke(TraceContext.ATO_PARENT_SPAN_ID, parentSpanId);
                    ReflectUtils
                            .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                            .invoke(TraceContext.ATO_SPAN_ID, spanId);
                    ReflectUtils
                            .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                            .invoke(TraceContext.ATO_TRACE_ID, traceId);
                    ReflectUtils
                            .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                            .invoke(TraceContext.ATO_PARENT_SPAN_ID, parentSpanId);
                    ReflectUtils
                            .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                            .invoke(TraceContext.ATO_SPAN_ID, spanId);
                }
            }
        }

    }
}
