package com.github.artlibs.autotrace4j.core;

/**
 * 功能：Morph callable for modifying the origin method
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public interface MorphCallable {
    /**
     * the original method call
     * @param args 方法参数表
     * @return 原方法执行结果
     */
    Object call(Object[] args);
}
