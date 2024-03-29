package com.github.artlibs.autotrace4j.core.interceptor;

import com.github.artlibs.autotrace4j.core.InterceptorType;

/**
 * Abstract ASM Visitor Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class AbstractVisitorInterceptor implements Interceptor {
    /**
     * 指明Visitor类，自己实现Visitor代码
     * @return visitor Class
     */
    public abstract Class<?> visitor();

    /**
     * 增强方式，VISITOR 或者 方法代理
     * @return EnhanceType
     */
    @Override
    public InterceptorType interceptType() {
        return InterceptorType.VISITOR;
    }
}
