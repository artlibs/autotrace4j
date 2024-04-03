package com.github.artlibs.autotrace4j.interceptor.base;

import com.github.artlibs.autotrace4j.interceptor.Interceptor;

import java.lang.reflect.Method;

/**
 * Abstract Visitor Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractVisitorInterceptor implements Interceptor<Object> {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisitorMode() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object obj, Object[] allArgs, Method originMethod) throws Exception {
        throw new UnsupportedOperationException("Visitor mode not support to this method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        throw new UnsupportedOperationException("Visitor mode not support to this method");
    }
}
