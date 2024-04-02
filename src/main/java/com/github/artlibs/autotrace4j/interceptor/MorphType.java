package com.github.artlibs.autotrace4j.interceptor;

/**
 * Morph callable Type
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface MorphType {
    /**
     * the original method call
     * @param args argument
     * @return result
     */
    Object call(Object[] args);
}
