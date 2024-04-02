package com.github.artlibs.autotrace4j.core;

/**
 * Morph callable Type
 *
 * @author Fury
 * @since 2023-01-04
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
