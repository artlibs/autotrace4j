package io.github.artlibs.autotrace4j.interceptor.base;

import io.github.artlibs.autotrace4j.interceptor.MorphCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract Static Delegate Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractStaticInterceptor extends AbstractDelegateInterceptor<Class<?>> {
    /**
     * intercept <code>io.github.artlibs.autotrace4j.support.Constants.INTERCEPT_METHOD_NAME</code>
     * @param clazz the class object
     * @param zuper the original object
     * @param args argument list
     * @param originMethod original method
     * @return result
     */
    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @Morph MorphCall zuper
            , @AllArguments Object[] args, @Origin Method originMethod) {
        return this.doIntercept(clazz, zuper, args, originMethod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractStaticInterceptor) {
            return Objects.equals(obj.hashCode(), this.hashCode());
        }
        return false;
    }
}
