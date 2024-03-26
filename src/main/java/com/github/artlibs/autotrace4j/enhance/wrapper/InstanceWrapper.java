package com.github.artlibs.autotrace4j.enhance.wrapper;

import com.github.artlibs.autotrace4j.enhance.MorphCallable;
import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
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
public class InstanceWrapper extends DelegateWrapper<Object> {
    private static final Map<Instance, InstanceWrapper>
            WRAPPER_MAP = new ConcurrentHashMap<>();

    public InstanceWrapper(Instance enhancer) throws Exception {
        super(enhancer);
    }

    public static InstanceWrapper wrap(Instance enhancer) throws Exception {
        InstanceWrapper wrapper = WRAPPER_MAP.get(Objects.requireNonNull(enhancer));
        if (Objects.nonNull(wrapper)) {
            return wrapper;
        }

        wrapper = new InstanceWrapper(enhancer);
        WRAPPER_MAP.put(enhancer, wrapper);

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
