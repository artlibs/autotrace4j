package com.github.artlibs.autotrace4j.interceptor.base;

import com.github.artlibs.autotrace4j.interceptor.MorphCall;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * Abstract Instance Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public abstract class AbstractInstanceInterceptor extends AbstractDelegateInterceptor<Object> {
    /**
     *
     * @param thiz the object
     * @param zuper the original object
     * @param args argument list
     * @param originMethod original method
     * @return result
     */
    @RuntimeType
    public Object intercept(@This Object thiz, @Morph MorphCall zuper
            , @AllArguments Object[] args, @Origin Method originMethod) {
        return this.doIntercept(thiz, zuper, args, originMethod);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object interceptor) {
        return super.equals(interceptor);
    }
}
