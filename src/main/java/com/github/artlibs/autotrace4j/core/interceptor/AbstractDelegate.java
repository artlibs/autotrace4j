package com.github.artlibs.autotrace4j.core.interceptor;

import com.github.artlibs.autotrace4j.core.InterceptorType;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract Delegate Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractDelegate<T> implements Interceptor {
    /**
     * 是否是增强static方法
     * @return true or false
     */
    public abstract boolean enhanceStaticMethod();

    /**
     * 在原方法刚开始进入时执行
     * @param classOrThis 增强的方法所在类或实例
     * @param allArgs 原方法的参数表
     * @param originMethod 原方法
     * @throws Exception -
     */
    public abstract void beforeMethod(T classOrThis, Object[] allArgs, Method originMethod) throws Exception;

    /**
     * 在原方法返回前执行
     * @param classOrThis 增强的方法所在类或实例
     * @param allArgs 原方法的参数表
     * @param result 方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    public abstract Object afterMethod(T classOrThis, Object[] allArgs, Object result, Method originMethod) throws Exception;

    /**
     * 增强方式，VISITOR 或者 方法代理
     * @return EnhanceType
     */
    @Override
    public InterceptorType interceptType() {
        return InterceptorType.DELEGATE;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + methodMatcher().hashCode() + typeMatcher().hashCode();
    }

    @Override
    public boolean equals(Object enhancer) {
        if (enhancer instanceof AbstractStatic) {
            return Objects.equals(enhancer.hashCode(), this.hashCode());
        }
        return false;
    }
}
