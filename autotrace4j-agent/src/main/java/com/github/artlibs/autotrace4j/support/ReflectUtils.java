package com.github.artlibs.autotrace4j.support;

import com.github.artlibs.autotrace4j.ctx.ReflectUtil;

import java.lang.reflect.Field;

/**
 * 功能：反射工具包
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public final class ReflectUtils {
    private ReflectUtils(){}

    public static WrappedMethod getMethod(String className, String methodName, Class<?>... parameterTypes) {
        return new WrappedMethod(ReflectUtil.getMethod(className, methodName, parameterTypes));
    }

    public static WrappedMethod getMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        return new WrappedMethod(obj, methodName, parameterTypes);
    }

    public static Field getField(Object obj, String fieldName) {
        return ReflectUtil.getField(obj, fieldName);
    }

    public static <T> T getFieldValue(Object obj, String fieldName) {
        return ReflectUtil.getFieldValue(obj, fieldName);
    }

    public static void setFieldValue(String className, String fieldName, Object value) {
        ReflectUtil.setFieldValue(className, fieldName, value);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        ReflectUtil.setFieldValue(obj, fieldName, value);
    }

    public static class WrappedMethod {
        private final ReflectUtil.WrapMethod proxy;

        private WrappedMethod(ReflectUtil.WrapMethod proxy) {
            this.proxy = proxy;
        }

        public WrappedMethod(Object obj, String methodName, Class<?>... parameterTypes) {
            this.proxy = ReflectUtil.WrapMethod.of(obj, methodName, parameterTypes);
        }

        public <T> T invoke(Object... args) {
            return this.proxy.invoke(args);
        }
    }
}
