package com.github.artlibs.autotrace4j.support;

import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能：java模块工具类
 *
 * @author suopovate
 * @since 2024/04/04
 * <p>
 * All rights Reserved.
 */
public class JavaModuleUtils {

    private JavaModuleUtils() {}

    public static final String[] AGENT_NECESSARY_JAVA_BASE_PKGS = {
        "sun.net.www.protocol.http",
        "sun.net.www",
        "jdk.internal.loader",
        };

    public static Class<?> MODULE_CLASS;

    static {
        try {
            MODULE_CLASS = Class.forName("java.lang.Module");
        } catch (ClassNotFoundException e) {
            MODULE_CLASS = null; //jdk8-;
        }
    }

    public static boolean notJavaModule() {
        return MODULE_CLASS == null;
    }

    public static void openJavaBaseModuleForAnotherModule(String[] allPkgs, JavaModule anotherModule) {
        if (anotherModule == null) {
            return;
        }
        openJavaBaseModuleForAnotherModule(allPkgs, anotherModule.unwrap());
    }

    public static void openJavaBaseModuleForAnotherModule(String[] allPkgs, Object anotherModule) {
        if (notJavaModule() || anotherModule == null) {
            return;
        }
        try {
            Method implAddOpensMethod = MODULE_CLASS.getDeclaredMethod("implAddOpens", String.class, MODULE_CLASS);
            // unlock method
            unlockMethod(implAddOpensMethod);
            for (String pkg : allPkgs) {
                implAddOpensMethod.invoke(getJdkBaseModule(), pkg, anotherModule);
            }
        } catch (Exception ignore) {}
    }

    private static void unlockMethod(Method implAddOpensMethod) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);

            Method objectFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
            long objectFieldOffset = (long) objectFieldOffsetMethod.invoke(
                unsafe,
                MethodLockSupport.class.getDeclaredField("first")
            );

            Method putBooleanVolatileMethod = unsafeClass.getDeclaredMethod(
                "putBooleanVolatile",
                Object.class, long.class, boolean.class
            );
            putBooleanVolatileMethod.invoke(unsafe, implAddOpensMethod, objectFieldOffset, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * note: this method need the privilege of 'jdk.internal.loader' package
     */
    public static void removePkgModuleMapping(String[] pkgs) {
        if (pkgs == null || pkgs.length == 0) {
            return;
        }
        try {
            // before call this method, we must open the 'jdk.internal.loader' module to current module
            Field packageToModuleField = ClassUtils.class
                .getClassLoader()
                .loadClass("jdk.internal.loader.BuiltinClassLoader")
                .getDeclaredField("packageToModule");
            packageToModuleField.setAccessible(true);
            ConcurrentHashMap<?, ?> packageToModule = (ConcurrentHashMap<?, ?>) packageToModuleField.get(null);
            for (String pkg : pkgs) {
                packageToModule.remove(pkg);
            }
        } catch (Exception ignore) {}

    }

    /**
     * return the own module or null if it cannot be found
     */
    public static Object getOwnModule(String locateClass) {
        try {
            return ClassUtils.getMethod(Class.class, "getModule").invoke(Class.forName(locateClass));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * call public api: ModuleLayer.boot().findModule("java.base").get();
     * but use reflection because we don't want this code to crash on jdk1.7 and below.
     * In that case, none of this stuff was needed in the first place, so we just exit via
     * the catch block and do nothing.
     */
    private static Object getJdkBaseModule() {
        try {
            Class<?> cModuleLayer = Class.forName("java.lang.ModuleLayer");
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = Class.forName("java.util.Optional");
            Method mFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, "java.base");
            return cOptional.getDeclaredMethod("get").invoke(oCompilerO);
        } catch (Exception e) {
            return null;
        }
    }

    public static class MethodLockSupport {
        boolean first;
    }

}
