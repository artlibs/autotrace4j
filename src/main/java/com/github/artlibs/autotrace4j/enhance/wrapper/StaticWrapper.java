package com.github.artlibs.autotrace4j.enhance.wrapper;

import com.github.artlibs.autotrace4j.enhance.MorphCallable;
import com.github.artlibs.autotrace4j.enhance.interceptor.Static;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能：
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class StaticWrapper extends DelegateWrapper<Class<?>> {
    private static final Map<Static, StaticWrapper>
            WRAPPER_MAP = new ConcurrentHashMap<>();

    private StaticWrapper(Static enhancer) throws Exception {
        super(enhancer);
    }

    public static StaticWrapper wrap(Static enhancer) throws Exception {
        StaticWrapper wrapper = WRAPPER_MAP.get(Objects.requireNonNull(enhancer));
        if (Objects.nonNull(wrapper)) {
            return wrapper;
        }

        wrapper = new StaticWrapper(enhancer);
        WRAPPER_MAP.put(enhancer, wrapper);

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
    public Object intercept(@Origin Class<?> clazz, @Morph MorphCallable callable
            , @AllArguments Object[] allArgs, @Origin Method originMethod) throws Exception {
        return this.enhance(clazz, callable, allArgs, originMethod);
    }
}
