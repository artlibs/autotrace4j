package com.github.artlibs.autotrace4j.interceptor.impl.jdk;

import com.github.artlibs.autotrace4j.interceptor.common.AbstractCallbackVisitorInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * ForkJoinPool Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class ForkJoinTaskInterceptor extends AbstractCallbackVisitorInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.util.concurrent.ForkJoinTask");
    }

    @Override
    protected ElementMatcher<? super MethodDescription> traceInjectedMethod() {
        return named("doExec");
    }

}
