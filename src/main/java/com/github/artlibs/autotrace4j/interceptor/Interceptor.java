package com.github.artlibs.autotrace4j.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Method;


/**
 * Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface Interceptor<T> {
    /**
     * visitor mode or not
     * @return true if visitor mode else method delegation
     */
    boolean isVisitorMode();

    /**
     * type matcher
     * @return ElementMatcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * method matcher
     * @return ElementMatcher
     */
    ElementMatcher<? super MethodDescription> methodMatcher();

    /**
     * enhance on method enter
     * @param obj thiz or class
     * @param allArgs argument list
     * @param originMethod original method
     * @throws Exception -
     */
    default void onMethodEnter(T obj, Object[] allArgs, Method originMethod) throws Exception {
        // NO Sonar
    }

    /**
     * enhance on method exit
     * @param obj thiz or class
     * @param allArgs argument list
     * @param result method result
     * @param originMethod original method
     * @return Object - result
     * @throws Exception -
     */
    default Object onMethodExit(T obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        return result;
    }

    /**
     * do type transform
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param module JavaModule
     * @return new DynamicType.Builder
     */
    default DynamicType.Builder<?> doTypeTransform(DynamicType.Builder<?> builder
            , TypeDescription typeDescription, JavaModule module, ClassLoader classLoader) {
        return builder;
    }

    /**
     * do intercept
     * @param obj class or thiz
     * @param zuper morph super
     * @param args argument list
     * @param method original method
     * @return result
     */
    default Object doIntercept(T obj, MorphCall zuper, Object[] args, Method method) {
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
}
