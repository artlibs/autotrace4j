package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.interceptor.common.AbstractServletInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * HTTP Filter Interceptor
 *
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class HttpFilterInterceptor extends AbstractServletInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("javax.servlet.Filter"))
                .and(not(isInterface())).and(not(isAbstract()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("doFilter")
            .and(takesArgument(
                0,
                named("javax.servlet.ServletRequest").or(hasSuperType(named("javax.servlet.ServletRequest")))
            ))
            .and(takesArgument(
                1,
                named("javax.servlet.ServletResponse").or(hasSuperType(named("javax.servlet.ServletResponse")))
            ))
            .and(takesArgument(2, hasSuperType(named("javax.servlet.FilterChain"))));
    }

}
