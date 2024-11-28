package io.github.artlibs.autotrace4j.interceptor;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.none;


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
     * The type matcher which is used to find the type to be intercepted.
     * @return the type matcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * do type transform
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param module JavaModule
     * @return new DynamicType.Builder
     */
    default DynamicType.Builder<?> typeTransformer(
        DynamicType.Builder<?> builder,
        TypeDescription typeDescription,
        JavaModule module,
        ClassLoader classLoader) {
        return builder;
    }

}
