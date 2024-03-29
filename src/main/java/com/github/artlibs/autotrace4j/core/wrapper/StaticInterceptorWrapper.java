package com.github.artlibs.autotrace4j.core.wrapper;

import com.github.artlibs.autotrace4j.core.Callable;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractStaticInterceptor;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static Interceptor Wrapper
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class StaticInterceptorWrapper extends AbstractDelegateWrapper<Class<?>> {
    private static final Map<AbstractStaticInterceptor, StaticInterceptorWrapper>
            Cache = new ConcurrentHashMap<>();

    private StaticInterceptorWrapper(AbstractStaticInterceptor enhancer) throws Exception {
        super(enhancer);
    }

    public static StaticInterceptorWrapper wrap(AbstractStaticInterceptor enhancer) throws Exception {
        StaticInterceptorWrapper wrapper = Cache.get(Objects.requireNonNull(enhancer));
        if (Objects.nonNull(wrapper)) {
            return wrapper;
        }

        wrapper = new StaticInterceptorWrapper(enhancer);
        Cache.put(enhancer, wrapper);

        return wrapper;
    }

    /**
     * 增强代码
     * @param clazz 增强方法所在类
     * @param callable 原方法 callable
     * @param allArgs 增强方法的参数表
     * @param originMethod 原方法
     * @return method execute result
     * @throws Exception -
     */
    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @Morph Callable callable
            , @AllArguments Object[] allArgs, @Origin Method originMethod) throws Exception {
        return this.enhance(clazz, callable, allArgs, originMethod);
    }
}
