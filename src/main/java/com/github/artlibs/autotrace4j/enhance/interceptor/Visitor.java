package com.github.artlibs.autotrace4j.enhance.interceptor;

import com.github.artlibs.autotrace4j.enhance.InterceptType;

/**
 * 功能：ASM访问者模式增强,推荐
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public abstract class Visitor implements Interceptor {
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
    public InterceptType interceptType() {
        return InterceptType.VISITOR;
    }
}
