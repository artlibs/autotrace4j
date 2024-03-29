package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.interceptor.AbstractServletInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 功能：Filter增强
 *
 * @author suopovate
 * @since 2024/03/25
 * <p>
 * All rights Reserved.
 */
public class HttpFilterInterceptor extends AbstractServletInterceptor {

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("javax.servlet.Filter")).and(not(isInterface())).and(not(isAbstract()));
    }

    /**
     * 方法匹配器
     * 匹配所有的Filter接口的实现类
     * <p>
     *
     * @return ElementMatcher
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
