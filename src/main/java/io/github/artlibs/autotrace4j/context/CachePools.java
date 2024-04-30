package io.github.artlibs.autotrace4j.context;

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

    /** field cache */
    private static final Map<String, Field> FIELD_POOL_CACHE = new ConcurrentHashMap<>();
    /** method cache */
    private static final Map<String, Method> METHOD_POOL_CACHE = new ConcurrentHashMap<>();

    /**
     * get field from cache
     * @param key field name
     * @return Field
     */
    public static Field getFieldCache(String key) {
        return FIELD_POOL_CACHE.get(key);
    }

    /**
     * cache field
     * @param key field name
     * @param field Field
     */
    public static void setFieldCache(String key, Field field) {
        FIELD_POOL_CACHE.put(key, field);
    }

    /**
     * get method from cache
     * @param key method name
     * @return Method
     */
    public static Method getMethodCache(String key) {
        return METHOD_POOL_CACHE.get(key);
    }

    /**
     * cache method
     * @param key method name
     * @param method Method
     */
    public static void setMethodCache(String key, Method method) {
        METHOD_POOL_CACHE.put(key, method);
    }
}
