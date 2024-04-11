package com.github.artlibs.autotrace4j.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;


/**
 * Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface Interceptor {
    /**
     * visitor mode or not
     * @return true if visitor mode else method delegation
     */
    boolean isVisitorMode();

    /**
     * type matcher
     * @return ElementMatcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * method matcher
     * @return ElementMatcher
     */
    ElementMatcher<? super MethodDescription> methodMatcher();
}
