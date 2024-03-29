package com.github.artlibs.autotrace4j.core.wrapper;

import com.github.artlibs.autotrace4j.core.MorphCallable;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractInstanceInterceptor;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instance Interceptor Wrapper
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class InstanceInterceptorWrapper extends AbstractDelegateWrapper<Object> {
    private static final Map<AbstractInstanceInterceptor, InstanceInterceptorWrapper>
            Cache = new ConcurrentHashMap<>();

    public InstanceInterceptorWrapper(AbstractInstanceInterceptor enhancer) throws Exception {
        super(enhancer);
    }

    public static InstanceInterceptorWrapper wrap(AbstractInstanceInterceptor enhancer) throws Exception {
        InstanceInterceptorWrapper wrapper = Cache.get(Objects.requireNonNull(enhancer));
        if (Objects.nonNull(wrapper)) {
            return wrapper;
        }

        wrapper = new InstanceInterceptorWrapper(enhancer);
        Cache.put(enhancer, wrapper);

        return wrapper;
    }

    /**
     * 增强代码
     * @param thiz 增强对象实例
     * @param callable 原方法 callable
     * @param allArgs 增强方法的参数表
     * @param originMethod 原方法
     * @return method execute result
     * @throws Exception -
     */
    @RuntimeType
    public Object intercept(@This Object thiz, @Morph MorphCallable callable
            , @AllArguments Object[] allArgs, @Origin Method originMethod) throws Exception {
        return this.enhance(thiz, callable, allArgs, originMethod);
    }
}
