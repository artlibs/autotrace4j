package io.github.artlibs.autotrace4j.interceptor.base;

import io.github.artlibs.autotrace4j.interceptor.Interceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * ASM Visitor
 *
 * @author Fury
 * @author suopovate
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractVisitorInterceptor implements Interceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isVisitorMode() {
        return true;
    }

    /**
     * build matcher map
     * @param matcher -
     * @return -
     */
    public final Map<Class<?>, ElementMatcher<? super MethodDescription>> ofMatcher(
            ElementMatcher<? super MethodDescription> matcher) {
        return ofMatcher(this.getClass(), matcher);
    }

    /**
     * build matcher map
     * @param adviceLocationClass -
     * @param matcher -
     * @return -
     */
    public final Map<Class<?>, ElementMatcher<? super MethodDescription>> ofMatcher(
            Class<?> adviceLocationClass, ElementMatcher<? super MethodDescription> matcher) {
        return newMmHolder().put(adviceLocationClass, matcher).get();
    }

    /**
     * new method matcher holder
     * @return -
     */
    public final MethodMatcherHolder newMmHolder() {
        return new MethodMatcherHolder();
    }

    /**
     * build matchers map
     * @return -
     */
    public abstract Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers();

    /**
     * MethodMatcherHolder
     */
    public static class MethodMatcherHolder {
        /** just a holder map */
        private final Map<Class<?>, ElementMatcher<? super MethodDescription>> matcherMap = new HashMap<>(8);

        /**
         * put visitor class with method matcher
         * @param adviceLocationClass -
         * @param matcher -
         * @return -
         */
        public final MethodMatcherHolder put(
                Class<?> adviceLocationClass, ElementMatcher<? super MethodDescription> matcher) {
            matcherMap.put(adviceLocationClass, matcher);
            return this;
        }

        /**
         * get the map
         * @return -
         */
        public Map<Class<?>, ElementMatcher<? super MethodDescription>> get() {
            return matcherMap;
        }
    }
}
