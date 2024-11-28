package io.github.artlibs.autotrace4j.transformer;

/**
 * Callable bind to @Morph
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface MorphCallable {
    /**
     * Override Callable
     * @param args argument
     * @return result
     */
    Object call(Object[] args);
}
