package com.github.artlibs.autotrace4j.interceptor.impl.jdk;

import com.github.artlibs.autotrace4j.interceptor.common.AbstractTaskVisitorInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 功能：ForkJoinTaskInterceptor
 *
 * @author Fury
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class ForkJoinTaskInterceptor extends AbstractTaskVisitorInterceptor {

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
