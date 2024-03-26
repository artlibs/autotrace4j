package com.github.artlibs.autotrace4j.ctx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能：反射工具包
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public final class ReflectUtil {
    private ReflectUtil(){}

    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    public static WrapMethod getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return new WrapMethod(clazz, methodName, parameterTypes);
    }

    public static WrapMethod getMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        return new WrapMethod(obj, methodName, parameterTypes);
    }

    public static Field getField(Object obj, String fieldName) {
        if (Objects.isNull(obj)) {
            return null;
        }

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        String cacheKey = clazz.getName() + "." + fieldName;
        Field field = FIELD_CACHE.get(cacheKey);
        if (Objects.isNull(field)) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                FIELD_CACHE.put(cacheKey, field);
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

    public static class WrapMethod {
        private Method method;
        private Object object;

        private WrapMethod(Object obj, String methodName, Class<?>... parameterTypes) {
            if (Objects.isNull(obj)) {
                return;
            }
            this.object = obj;

            Class<?> clazz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
            try {
                final String key = clazz.getName() + "." + methodName;
                method = METHOD_CACHE.get(key);
                if (Objects.isNull(method)) {
                    method = clazz.getMethod(methodName, parameterTypes);
                    method.setAccessible(true);
                    METHOD_CACHE.put(key, method);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static WrapMethod of(Object obj, String methodName, Class<?>... parameterTypes) {
            return new WrapMethod(obj, methodName, parameterTypes);
        }

        @SuppressWarnings("unchecked")
        public <T> T invoke(Object... args) {
            if (Objects.isNull(object)) {
                return null;
            }

            try {
                return (T)method.invoke(object, args);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
