package com.github.artlibs.autotrace4j.enhance.wrapper;

import com.github.artlibs.autotrace4j.enhance.MorphCallable;
import com.github.artlibs.autotrace4j.enhance.interceptor.Delegate;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 功能：
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class DelegateWrapper<T> {
    private final Delegate<T> enhancer;

    protected DelegateWrapper(Delegate<T> enhancer) throws Exception {
        this.enhancer = Objects.requireNonNull(enhancer);
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
    protected Object enhance(T classOrThis, MorphCallable callable, Object[] allArgs, Method originMethod) throws Exception {
        try {
            enhancer.beforeMethod(classOrThis, allArgs, originMethod);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        Object result = null;
        try {
            result = callable.call(allArgs);
        } finally {
            try {
                // 当 callable.call 发生异常时, 虽然这里 result 可能发生变更, 但该值变更已无意义
                result = enhancer.afterMethod(classOrThis, allArgs, result, originMethod);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        return result;
    }
}
