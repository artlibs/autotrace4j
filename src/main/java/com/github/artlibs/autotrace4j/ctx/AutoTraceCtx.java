package com.github.artlibs.autotrace4j.ctx;

import java.util.UUID;

/**
 * Trace context for SpanId, ParentSpanId, TraceId
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public final class AutoTraceCtx {
    private AutoTraceCtx() {}

    /** TraceId，贯穿全链路 */
    private static final ThreadLocal<String> TRACE_ID_CTX = new ThreadLocal<>();

    /** SpanId，跟踪当前上下文 */
    private static final ThreadLocal<String> SPAN_ID_CTX = new ThreadLocal<>();

    /** ParentSpanId，上游上下文SpanId，串联上游上下文 */
    private static final ThreadLocal<String> PARENT_SPAN_ID_CTX = new ThreadLocal<>();

    /** 注入到HTTP header/response的字段名 */
    public static final String TRACE_HEADER = "traceId";
    public static final String ATO_TRACE_ID = "X-ATO-TRACE-ID";
    public static final String ATO_SPAN_ID = "X-ATO-SPAN-ID";
    public static final String ATO_PARENT_SPAN_ID = "X-ATO-P-SPAN-ID";

    /** 注入到logger日志的字段名 */
    public static final String TRACE_KEY = "autoTraceId";
    public static final String TRACE_KEY_GETTER = "getAutoTraceId";
    public static final String TRACE_KEY_SETTER = "setAutoTraceId";

    /** 注入到logger日志的字段名 */
    public static final String SPAN_KEY = "autoSpanId";
    public static final String SPAN_KEY_GETTER = "getAutoSpanId";
    public static final String SPAN_KEY_SETTER = "setAutoSpanId";

    /** 注入到logger日志的字段名 */
    public static final String PARENT_SPAN_KEY = "autoParentSpanId";
    public static final String PARENT_SPAN_KEY_GETTER = "getAutoParentSpanId";
    public static final String PARENT_SPAN_KEY_SETTER = "setAutoParentSpanId";

    public static String getTraceId() {
        return TRACE_ID_CTX.get();
    }

    public static void setTraceId(String traceId) {
        TRACE_ID_CTX.set(traceId);
    }

    public static void removeTraceId() {
        TRACE_ID_CTX.remove();
    }

    public static String getSpanId() {
        return SPAN_ID_CTX.get();
    }

    public static void setSpanId(String spanId) {
        SPAN_ID_CTX.set(spanId);
    }

    public static void removeSpanId() {
        SPAN_ID_CTX.remove();
    }

    public static String getParentSpanId() {
        return PARENT_SPAN_ID_CTX.get();
    }

    public static void setParentSpanId(String parentSpanId) {
        PARENT_SPAN_ID_CTX.set(parentSpanId);
    }

    public static void removeParentSpanId() {
        PARENT_SPAN_ID_CTX.remove();
    }

    public static void removeAll() {
        TRACE_ID_CTX.remove();
        SPAN_ID_CTX.remove();
        PARENT_SPAN_ID_CTX.remove();
    }

    public static String generate() {
        return UUID.randomUUID()
            .toString()
            .substring(18)
            .replace("-", "");
    }
}
