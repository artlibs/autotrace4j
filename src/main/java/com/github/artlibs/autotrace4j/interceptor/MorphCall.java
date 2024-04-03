package com.github.artlibs.autotrace4j.interceptor;

/**
 * Override Callable bind to @Morph
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface MorphCall {
    /**
     * Override Callable
     * @param args argument
     * @return result
     */
    Object call(Object[] args);
}
