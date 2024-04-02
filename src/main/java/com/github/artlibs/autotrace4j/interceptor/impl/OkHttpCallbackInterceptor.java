package com.github.artlibs.autotrace4j.interceptor.impl;

import com.github.artlibs.autotrace4j.interceptor.Transformer;
import com.github.artlibs.autotrace4j.interceptor.common.AbstractCallbackInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * OkHttp3 Callback Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class OkHttpCallbackInterceptor extends AbstractCallbackInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers.named("okhttp3.Callback"))
                .and(Transformer.getInterceptScopeJunction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("onResponse").or(ElementMatchers.named("onFailure"));
    }

}
