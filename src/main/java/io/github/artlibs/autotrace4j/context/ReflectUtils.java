package io.github.artlibs.autotrace4j.context;

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
     * Get [public] methods from the object's class, or it's super class.
     * @param obj object or class
     * @param methodName method
     * @param parameterTypes arg types
     * @return method wrapper
     */
    public static MethodWrapper getMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        return MethodWrapper.of(obj, methodName, false, parameterTypes);
    }

    /**
     * Get [declared] methods of the object's class.
     * @param obj object or class
     * @param methodName method
     * @param parameterTypes arg types
     * @return method wrapper
     */
    public static MethodWrapper getDeclaredMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        return MethodWrapper.of(obj, methodName, true, parameterTypes);
    }

    /**
     * Get [public] fields from the object/class, or it's super/class.
     * @param obj object or class
     * @param fieldName field
     * @return field
     */
    public static Field getField(Object obj, String fieldName) {
        return getField(obj, fieldName, false);
    }

    /**
     * Get [declared] fields from the object or class.
     * @param obj object or class
     * @param fieldName field
     * @return field
     */
    public static Field getDeclaredField(Object obj, String fieldName) {
        return getField(obj, fieldName, true);
    }

    /**
     * Get fields from the object/class (or it's super/class).
     * @param obj object or class
     * @param fieldName field
     * @param declared declared method or not
     * @return field
     */
    private static Field getField(Object obj, String fieldName, boolean declared) {
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

    /**
     * Get [public] field value from the object/class, or it's super/class.
     * @param obj object or class
     * @param fieldName field
     * @return result
     * @param <T> field type
     */
    public static <T> T getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, fieldName, false);
    }

    /**
     * Get [declared] field value from the object or class.
     * @param obj object or class
     * @param fieldName field
     * @return result
     * @param <T> field type
     */
    public static <T> T getDeclaredFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj, fieldName, true);
    }

    /**
     * Get field value from the object/class (or it's super/class).
     * @param obj object or class
     * @param fieldName field
     * @param declared declared method or not
     * @return result
     * @param <T> field type
     */
    @SuppressWarnings("unchecked")
    private static <T> T getFieldValue(Object obj, String fieldName, boolean declared) {
        Field field = getField(obj, fieldName, declared);
        try {
            return Objects.isNull(field) ? null : (T) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set [public] field value for the object/class (or it's super/class).
     * @param obj object or class
     * @param fieldName field
     * @param value field value
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, fieldName, value, false);
    }

    /**
     * Set [declared] field value for the object (or class).
     * @param obj object or class
     * @param fieldName field
     * @param value field value
     */
    public static void setDeclaredFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, fieldName, value, true);
    }

    /**
     * Set field value for the object/class, or it's super/class.
     * @param obj object or class
     * @param fieldName field
     * @param value field value
     * @param declared declared method or not
     */
    private static void setFieldValue(Object obj, String fieldName, Object value, boolean declared) {
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
