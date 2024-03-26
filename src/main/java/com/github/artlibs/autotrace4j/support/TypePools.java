package com.github.artlibs.autotrace4j.support;

/**
 * 功能：Type Pools
 *
 * @author Fury
 * @since 2023-01-05
 *
 * All rights Reserved.
 */
public final class TypePools {
    private TypePools(){}

    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";
    public static final String FILE_PROTOCOL = "file:";
    public static final String JAR_SUFFIX = ".jar";
    public static final String AUTO_TRACE_CORE = "auto-trace";
    public static final String AUTO_TRACE_SPR = "auto-trace-spring";
    public static final String AUTO_TRACE_CTX = "auto-trace-ctx";
    public static final String GET_HEADER = "getHeader";
    public static final String SET_HEADER = "setHeader";

    /**
     * System class loader type pools
     * @return net.bytebuddy.pool.TypePool
     */
    public static net.bytebuddy.pool.TypePool getSystemLoaderPool() {
        return net.bytebuddy.pool.TypePool.Default.ofSystemLoader();
    }
}
