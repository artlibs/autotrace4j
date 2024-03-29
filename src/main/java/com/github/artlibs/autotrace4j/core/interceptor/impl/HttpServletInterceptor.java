package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.interceptor.AbstractServletInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Http Servlet
 *
 * @author Fury
 * @since 2023-01-04
 * <p>
 * All rights Reserved.
 */
public class HttpServletInterceptor extends AbstractServletInterceptor {

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("javax.servlet.http.HttpServlet");
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("service")
            .and(takesArgument(0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
            .and(takesArgument(1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")));
    }
}
