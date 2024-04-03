package com.github.artlibs.autotrace4j.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache pools
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class CachePools {
    private CachePools() {}

    private static final Map<String, Field> FIELD_POOL_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_POOL_CACHE = new ConcurrentHashMap<>();

    public static Field getFieldCache(String key) {
        return FIELD_POOL_CACHE.get(key);
    }

    public static void setFieldCache(String key, Field field) {
        FIELD_POOL_CACHE.put(key, field);
    }

    public static Method getMethodCache(String key) {
        return METHOD_POOL_CACHE.get(key);
    }

    public static void setMethodCache(String key, Method method) {
        METHOD_POOL_CACHE.put(key, method);
    }
}