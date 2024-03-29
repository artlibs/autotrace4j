package com.github.artlibs.autotrace4j.ctx;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Core Reflect Utils
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public final class ReflectUtils {
    private ReflectUtils(){}

    public static MethodWrapper getMethodWrapper(String className, String methodName, Class<?>... parameterTypes) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MethodWrapper.of(clazz, methodName, parameterTypes);
    }

    public static MethodWrapper getMethodWrapper(Object obj, String methodName, Class<?>... parameterTypes) {
        return MethodWrapper.of(obj, methodName, parameterTypes);
    }

    public static Field getField(Object obj, String fieldName) {
        if (Objects.isNull(obj)) {
            return null;
        }

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        String cacheKey = clazz.getName() + "." + fieldName;
        Field field = CachePools.getFieldCache(cacheKey);
        if (Objects.isNull(field)) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                CachePools.setFieldCache(cacheKey, field);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return field;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Field field = getField(obj, fieldName);

        try {
            return Objects.isNull(field) ? null : (T)field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFieldValue(String className, String fieldName, Object value) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        setFieldValue(clazz, fieldName, value);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = getField(obj, fieldName);
        if (Objects.isNull(field)) {
            return;
        }

        try {
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
