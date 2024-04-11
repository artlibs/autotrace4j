package com.github.artlibs.autotrace4j.interceptor.base;

import com.github.artlibs.autotrace4j.interceptor.Interceptor;
import com.github.artlibs.autotrace4j.interceptor.MorphCall;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Method;

/**
 * Method Delegate
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractDelegateInterceptor<T> implements Interceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisitorMode() {
        return false;
    }

    /**
     * enhance on method enter
     * @param obj thiz or class
     * @param allArgs argument list
     * @param originMethod original method
     * @throws Exception -
     */
    public void onMethodEnter(T obj, Object[] allArgs, Method originMethod) throws Exception {
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
    public Object onMethodExit(T obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
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
    public DynamicType.Builder<?> doTypeTransform(DynamicType.Builder<?> builder
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
    protected Object doIntercept(T obj, MorphCall zuper, Object[] args, Method method) {
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

    @Override
    public int hashCode() {
        return super.hashCode() + methodMatcher().hashCode() + typeMatcher().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
