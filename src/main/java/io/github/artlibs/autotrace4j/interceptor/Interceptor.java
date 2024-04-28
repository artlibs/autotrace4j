package io.github.artlibs.autotrace4j.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;


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

    /**
     * do type transform
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param module JavaModule
     * @return new DynamicType.Builder
     */
    default DynamicType.Builder<?> doTypeTransform(
        DynamicType.Builder<?> builder,
        TypeDescription typeDescription,
        JavaModule module,
        ClassLoader classLoader
    ) {
        return builder;
    }

}
