package com.github.artlibs.autotrace4j.core.interceptor.base;

import com.github.artlibs.autotrace4j.core.MorphType;
import com.github.artlibs.autotrace4j.core.interceptor.Interceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract Static Delegate Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractStaticInterceptor implements Interceptor<Class<?>> {
    /**
     * {@inheritDoc}
     */
    public boolean isVisitorMode() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Class<?> clazz, Object[] allArgs, Method originMethod) throws Exception {
        // NO Sonar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Class<?> clazz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        return result;
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @Morph MorphType callable
            , @AllArguments Object[] allArgs, @Origin Method originMethod) {
        return this.doIntercept(clazz, callable, allArgs, originMethod);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + methodMatcher().hashCode() + typeMatcher().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractStaticInterceptor) {
            return Objects.equals(obj.hashCode(), this.hashCode());
        }
        return false;
    }
}
