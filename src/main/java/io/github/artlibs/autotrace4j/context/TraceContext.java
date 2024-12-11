package io.github.artlibs.autotrace4j.context;

import java.util.Objects;
import java.util.UUID;

/**
 * Trace context for SpanId, ParentSpanId, TraceId
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public final class TraceContext {
    private TraceContext() {}

    /** TraceId Context */
    private static final ThreadLocal<String> TRACE_ID_CTX = new ThreadLocal<>();
    /** SpanId Context */
    private static final ThreadLocal<String> SPAN_ID_CTX = new ThreadLocal<>();
    /** ParentSpanId Context */
    private static final ThreadLocal<String> PARENT_SPAN_ID_CTX = new ThreadLocal<>();

    /** HTTP request/response Header */
    public static final String ATO_TRACE_ID = "X-Ato-Trace-Id";
    /** HTTP request/response Header */
    public static final String ATO_SPAN_ID = "X-Ato-Span-Id";
    /** HTTP request/response Header */
    public static final String ATO_PARENT_SPAN_ID = "X-Ato-P-Span-ID";

    /** logger field */
    public static final String TRACE_KEY = "autoTraceId";
    /** logger field getter */
    public static final String TRACE_KEY_GETTER = "getAutoTraceId";
    /** logger field setter */
    public static final String TRACE_KEY_SETTER = "setAutoTraceId";

    /** logger field */
    public static final String SPAN_KEY = "autoSpanId";
    /** logger field getter */
    public static final String SPAN_KEY_GETTER = "getAutoSpanId";
    /** logger field setter */
    public static final String SPAN_KEY_SETTER = "setAutoSpanId";

    /** logger field */
    public static final String PARENT_SPAN_KEY = "autoParentSpanId";
    /** logger field getter */
    public static final String PARENT_SPAN_KEY_GETTER = "getAutoParentSpanId";
    /** logger field setter */
    public static final String PARENT_SPAN_KEY_SETTER = "setAutoParentSpanId";

    /**
     * get trace id from context
     * @return trace id
     */
    public static String getTraceId() {
        return TRACE_ID_CTX.get();
    }

    /**
     * set trace id to context
     * @param traceId -
     */
    public static void setTraceId(String traceId) {
        TRACE_ID_CTX.set(traceId);
    }

    /**
     * remove trace id from context
     */
    public static void removeTraceId() {
        TRACE_ID_CTX.remove();
    }

    /**
     * get span id from context
     * @return -
     */
    public static String getSpanId() {
        return SPAN_ID_CTX.get();
    }

    /**
     * set span id to context
     * @param spanId -
     */
    public static void setSpanId(String spanId) {
        SPAN_ID_CTX.set(spanId);
    }

    /**
     * remove span id from context
     */
    public static void removeSpanId() {
        SPAN_ID_CTX.remove();
    }

    /**
     * get parent span id from context
     * @return -
     */
    public static String getParentSpanId() {
        return PARENT_SPAN_ID_CTX.get();
    }

    /**
     * set parent span id to context
     * @param parentSpanId -
     */
    public static void setParentSpanId(String parentSpanId) {
        PARENT_SPAN_ID_CTX.set(parentSpanId);
    }

    /**
     * remove parent span id from context
     */
    public static void removeParentSpanId() {
        PARENT_SPAN_ID_CTX.remove();
    }

    /**
     * remove all context
     */
    public static void removeAll() {
        removeTraceId();
        removeSpanId();
        removeParentSpanId();
    }

    /**
     * generate id
     * @return -
     */
    public static String generate() {
        return UUID.randomUUID()
            .toString()
            .substring(18)
            .replace("-", "");
    }

    private static final String SEPARATOR = " - ";
    private static final String QUOTE_COLON = "\":\"";

    /**
     * Inject trace id into log message
     * @param message String
     * @return result
     */
    public static String injectTraceId(String message) {
        if (Objects.isNull(message) || Objects.isNull(TraceContext.getTraceId())) {
            return message;
        }

        try {
            String trimMessage = message.trim();

            // JSON FORMAT
            if (trimMessage.startsWith("{") && trimMessage.endsWith("}")) {
                return injectJsonString(message, trimMessage);
            }

            // TODO: XML FORMAT

            // TODO: HTML FORMAT

            return injectString(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    private static String injectString(String message) {
        if (message.trim().isEmpty() || message.contains("[TraceId]" + TraceContext.getTraceId())) {
            return message;
        }

        String preTrimMessage = "[TraceId]" + TraceContext.getTraceId() + SEPARATOR;
        if (Objects.nonNull(TraceContext.getSpanId())) {
            preTrimMessage += "[SpanId]" + TraceContext.getSpanId() + SEPARATOR;
        }
        if (Objects.nonNull(TraceContext.getParentSpanId())) {
            preTrimMessage += "[P-SpanId]" + TraceContext.getParentSpanId() + SEPARATOR;
        }

        return preTrimMessage + message;
    }

    private static String injectJsonString(String message, String trimMessage) {
        if (message.contains(TraceContext.ATO_TRACE_ID) && message.contains(TraceContext.getTraceId())) {
            return message;
        }

        String injectedTraceFields = "\"" + TraceContext.ATO_TRACE_ID + QUOTE_COLON + TraceContext.getTraceId() + "\",";
        if (Objects.nonNull(TraceContext.getSpanId())) {
            injectedTraceFields = injectedTraceFields
                    + "\"" + TraceContext.ATO_SPAN_ID + QUOTE_COLON + TraceContext.getSpanId() + "\",";
        }
        if (Objects.nonNull(TraceContext.getParentSpanId())) {
            injectedTraceFields = injectedTraceFields
                    + "\"" + TraceContext.ATO_PARENT_SPAN_ID + QUOTE_COLON + TraceContext.getParentSpanId() + "\",";
        }
        trimMessage = "{" + injectedTraceFields + trimMessage.substring(1);

        return message.endsWith("\n") ? trimMessage + "\n" : trimMessage;
    }
}
