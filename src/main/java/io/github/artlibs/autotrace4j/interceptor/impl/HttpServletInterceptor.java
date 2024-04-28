package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.interceptor.common.AbstractServletInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Http Servlet
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class HttpServletInterceptor extends AbstractServletInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("javax.servlet.http.HttpServlet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("service")
            .and(takesArgument(0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
            .and(takesArgument(1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")));
    }
}
