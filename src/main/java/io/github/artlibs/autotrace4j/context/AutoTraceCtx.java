package io.github.artlibs.autotrace4j.context;

import java.util.UUID;

/**
 * Trace context for SpanId, ParentSpanId, TraceId
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public final class AutoTraceCtx {
    private AutoTraceCtx() {}

    /** TraceId */
    private static final ThreadLocal<String> TRACE_ID_CTX = new ThreadLocal<>();

    /** SpanId */
    private static final ThreadLocal<String> SPAN_ID_CTX = new ThreadLocal<>();

    /** ParentSpanId */
    private static final ThreadLocal<String> PARENT_SPAN_ID_CTX = new ThreadLocal<>();

    /** HTTP header/response */
    public static final String ATO_TRACE_ID = "X-Ato-Trace-Id";
    public static final String ATO_SPAN_ID = "X-Ato-Span-Id";
    public static final String ATO_PARENT_SPAN_ID = "X-Ato-P-Span-ID";

    /** logger field */
    public static final String TRACE_KEY = "autoTraceId";
    public static final String TRACE_KEY_GETTER = "getAutoTraceId";
    public static final String TRACE_KEY_SETTER = "setAutoTraceId";

    /** logger field */
    public static final String SPAN_KEY = "autoSpanId";
    public static final String SPAN_KEY_GETTER = "getAutoSpanId";
    public static final String SPAN_KEY_SETTER = "setAutoSpanId";

    /** logger field */
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
        removeTraceId();
        removeSpanId();
        removeParentSpanId();
    }

    public static String generate() {
        return UUID.randomUUID()
            .toString()
            .substring(18)
            .replace("-", "");
    }
}
