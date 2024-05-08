package io.github.artlibs.autotrace4j.context;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Method Wrapper for avoiding NPE.
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class MethodWrapper {
    /** raw method */
    private Method method;
    /** raw instance */
    private Object object;

    /** using of instead */
    private MethodWrapper(){}

    /**
     * build method wrapper
     * @param obj object or class
     * @param methodName method
     * @param declared declared method or not
     * @param parameterTypes param types
     * @return Method Wrapper
     */
    public static MethodWrapper of(Object obj, String methodName, boolean declared, Class<?>... parameterTypes) {
        MethodWrapper wrapper = new MethodWrapper();
        if (Objects.isNull(obj)) {
            return wrapper;
        }
        wrapper.object = obj;

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        try {
            final String key = clazz.getName() + "." + methodName;
            wrapper.method = CachePools.getMethodCache(key);
            if (Objects.isNull(wrapper.method)) {
                wrapper.method = declared
                                 ? clazz.getDeclaredMethod(methodName, parameterTypes)
                                 : clazz.getMethod(methodName, parameterTypes);
                wrapper.method.setAccessible(true);
                CachePools.setMethodCache(key, wrapper.method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wrapper;
    }

    /**
     * invoke method on instance
     * @param args args
     * @return result
     * @param <T> type
     */
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
