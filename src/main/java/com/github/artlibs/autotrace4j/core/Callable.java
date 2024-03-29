package com.github.artlibs.autotrace4j.core;

/**
 * Morph callable
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public interface Callable {
    /**
     * the original method call
     * @param args 方法参数表
     * @return 原方法执行结果
     */
    Object call(Object[] args);
}
