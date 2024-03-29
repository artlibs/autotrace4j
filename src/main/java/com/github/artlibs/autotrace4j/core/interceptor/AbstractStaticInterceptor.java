package com.github.artlibs.autotrace4j.core.interceptor;

import java.lang.reflect.Method;

/**
 * Abstract Static Delegate Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractStaticInterceptor extends AbstractDelegateInterceptor<Class<?>> {
    /**
     * 是否是增强static方法
     * @return true or false
     */
    @Override
    public boolean enhanceStaticMethod() {
        return true;
    }

    /**
     * 在原方法刚开始进入时执行
     * @param clazz 增强的方法所在类
     * @param allArgs 原方法的参数表
     * @param originMethod 原方法
     * @throws Exception -
     */
    @Override
    public void beforeMethod(Class<?> clazz, Object[] allArgs, Method originMethod) throws Exception {}

    /**
     * 在原方法返回前执行
     * @param clazz 增强的方法所在类
     * @param allArgs 原方法的参数表
     * @param result 方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    @Override
    public Object afterMethod(Class<?> clazz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        return result;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object enhancer) {
        return super.equals(enhancer);
    }
}
