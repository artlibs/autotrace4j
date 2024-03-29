package com.github.artlibs.autotrace4j.core.wrapper;

import com.github.artlibs.autotrace4j.core.Callable;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractDelegateInterceptor;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract Delegate Wrapper
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractDelegateWrapper<T> {
    private final AbstractDelegateInterceptor<T> interceptor;

    protected AbstractDelegateWrapper(AbstractDelegateInterceptor<T> enhancer) throws Exception {
        this.interceptor = Objects.requireNonNull(enhancer);
    }

    /**
     * 增强代码
     * @param classOrThis 增强方法所在类或实例
     * @param callable 原方法 callable
     * @param allArgs 增强方法的参数表
     * @param originMethod 原方法
     * @return method execute result
     * @throws Exception -
     */
    protected Object enhance(T classOrThis, Callable callable, Object[] allArgs, Method originMethod) throws Exception {
        try {
            interceptor.beforeMethod(classOrThis, allArgs, originMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object result = null;
        try {
            result = callable.call(allArgs);
        } finally {
            try {
                result = interceptor.afterMethod(classOrThis, allArgs, result, originMethod);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
