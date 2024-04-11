package com.github.artlibs.autotrace4j.interceptor.base;

import com.github.artlibs.autotrace4j.interceptor.Interceptor;

/**
 * ASM Visitor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractVisitorInterceptor implements Interceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisitorMode() {
        return true;
    }
}
