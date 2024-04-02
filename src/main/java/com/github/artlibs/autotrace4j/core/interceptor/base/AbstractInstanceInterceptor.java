package com.github.artlibs.autotrace4j.core.interceptor.base;

import com.github.artlibs.autotrace4j.core.MorphType;
import com.github.artlibs.autotrace4j.core.interceptor.Interceptor;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * Abstract Instance Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractInstanceInterceptor implements Interceptor<Object> {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisitorMode() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        // NO Sonar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        return result;
    }

    @RuntimeType
    public Object intercept(@This Object thiz, @Morph MorphType callable
            , @AllArguments Object[] allArgs, @Origin Method originMethod) {
        return this.doIntercept(thiz, callable, allArgs, originMethod);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + methodMatcher().hashCode() + typeMatcher().hashCode();
    }

    @Override
    public boolean equals(Object interceptor) {
        return super.equals(interceptor);
    }
}
