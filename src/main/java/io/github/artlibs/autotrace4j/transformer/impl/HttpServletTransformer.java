package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Http Servlet 增强转换器
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class HttpServletTransformer extends AbsDelegateTransformer.AbsServlet {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("javax.servlet.http.HttpServlet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("service")
            .and(takesArgument(0, named("javax.servlet.http.HttpServletRequest")))
            .and(takesArgument(1, named("javax.servlet.http.HttpServletResponse")));
    }
}
