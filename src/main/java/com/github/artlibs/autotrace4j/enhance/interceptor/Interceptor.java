package com.github.artlibs.autotrace4j.enhance.interceptor;

import com.github.artlibs.autotrace4j.enhance.InterceptType;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 功能：拦截器
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public interface Interceptor {
    /**
     * 增强方式，VISITOR 或者 方法代理
     * @return EnhanceType
     */
    InterceptType interceptType();

    /**
     * 类型匹配器
     * @return ElementMatcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * 方法匹配器
     * @return ElementMatcher
     */
    ElementMatcher<? super MethodDescription> methodMatcher();

    /**
     * 类型转换，如增加字段、方法等
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @return new DynamicType.Builder
     */
    default DynamicType.Builder<?> transformType(DynamicType.Builder<?> builder
            , TypeDescription typeDescription, ClassLoader classLoader) {
        return builder;
    }
}
