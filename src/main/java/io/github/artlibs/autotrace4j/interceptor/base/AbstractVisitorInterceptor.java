package io.github.artlibs.autotrace4j.interceptor.base;

import io.github.artlibs.autotrace4j.interceptor.Interceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.none;

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

    @Override
    public final ElementMatcher<? super MethodDescription> methodMatcher() {
        return none();
    }

    public abstract DynamicType.Builder<?> visit(DynamicType.Builder<?> builder);

}
