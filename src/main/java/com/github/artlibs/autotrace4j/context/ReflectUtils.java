package com.github.artlibs.autotrace4j.context;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Core Reflect Utils
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class ReflectUtils {
    private ReflectUtils() {}

    /**
     * get public member from class or super class
     */
    public static MethodWrapper getMethodWrapper(String className, String methodName, Class<?>... parameterTypes) {
        return getMethodWrapper(className, methodName, false, parameterTypes);
    }

    /**
     * get public member from class or super class or get declared member form class
     */
    public static MethodWrapper getMethodWrapper(
        String className, String methodName, boolean declared, Class<?>... parameterTypes
    ) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MethodWrapper.of(clazz, methodName, declared, parameterTypes);
    }

    /**
     * get public member from object's class or super class
     */
    public static MethodWrapper getMethodWrapper(Object obj, String methodName, Class<?>... parameterTypes) {
        return getMethodWrapper(obj, methodName, false, parameterTypes);
    }

    /**
     * get public member from object's class or super class or get declared field form class
     */
    public static MethodWrapper getMethodWrapper(Object obj, String methodName, boolean declared, Class<?>... parameterTypes) {
        return MethodWrapper.of(obj, methodName, declared, parameterTypes);
    }

    /**
     * get public field from object's class or super class
     */
    public static Field getField(Object obj, String fieldName) {
        return getField(obj, fieldName, false);
    }

    /**
     * get public field from object's class or super class or get declared field form class
     */
    public static Field getField(Object obj, String fieldName, boolean declared) {
        if (Objects.isNull(obj)) {
            return null;
        }

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>) obj : obj.getClass();
        String cacheKey = clazz.getName() + "." + fieldName;
        Field field = CachePools.getFieldCache(cacheKey);
        if (Objects.isNull(field)) {
            try {
                field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
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
        Field field = getField(obj, fieldName, false);

        try {
            return Objects.isNull(field) ? null : (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName, boolean declared) {
        Field field = getField(obj, fieldName, declared);

        try {
            return Objects.isNull(field) ? null : (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFieldValue(String className, String fieldName, Object value) {
        setFieldValue(className, fieldName, value, false);
    }

    public static void setFieldValue(String className, String fieldName, Object value, boolean declared) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        setFieldValue(clazz, fieldName, value, declared);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, fieldName, value, false);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value, boolean declared) {
        Field field = getField(obj, fieldName, declared);
        if (Objects.isNull(field)) {
            return;
        }
        try {
            if (obj instanceof Class) {
                field.set(null, value);
            } else {
                field.set(obj, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
