package com.github.artlibs.autotrace4j.support;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;

import java.util.UUID;

/**
 * 功能：TraceUtils 工具类
 *
 * @author Fury
 * @since 2023-01-05
 *
 * All rights Reserved.
 */
public final class TraceUtils {
    private TraceUtils() {}

    /**
     * 设置当前上下文的 Trace Id
     */
    public static void setTraceId(String traceId) {
        try {
            AutoTraceCtx.setTraceId(traceId);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 获取当前上下文的 Trace Id
     * @return 当前上下文的 Trace Id
     */
    public static String getTraceId() {
        try {
            return AutoTraceCtx.getTraceId();
        } catch (Exception ignore) {
            // NO Sonar
        }
        return generate();
    }

    /**
     * 获取当前上下文的 Span Id
     * @return 当前上下文的 Span Id
     */
    public static String getSpanId() {
        try {
            return AutoTraceCtx.getSpanId();
        } catch (Exception ignore) {
            // NO Sonar
        }
        return "NO-SPAN-ID";
    }

    /**
     * 获取当前上下文的 parent span Id
     * @return 当前上下文的 parent Span Id
     */
    public static String getParentSpanId() {
        try {
            return AutoTraceCtx.getParentSpanId();
        } catch (Exception ignore) {
            // NO Sonar
        }
        return "NO-PARENT-SPAN-ID";
    }

    /**
     * 生成一个 Id
     * @return Id
     */
    public static String generate() {
        try {
            return AutoTraceCtx.generate();
        } catch (Exception ignore) {
            // NO Sonar
        }

        // Copy from AutoTraceCtx.generate()
        return UUID.randomUUID()
            .toString()
            .substring(18)
            .replace("-", "");
    }
}
