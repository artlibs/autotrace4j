package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Logback Encoder Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class LogbackEncoderTransformer extends AbsDelegateTransformer.Instance {
    private static final String SEPARATOR = " - ";
    private static final String QUOTE_COLON = "\":\"";
    private static final String LAYOUT_WRAPPING_ENCODER
            = "ch.qos.logback.core.encoder.LayoutWrappingEncoder";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named(LAYOUT_WRAPPING_ENCODER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("encode").and(
                isOverriddenFrom(named(LAYOUT_WRAPPING_ENCODER)).or(
                isDeclaredBy(named(LAYOUT_WRAPPING_ENCODER)) )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) {
        if (Objects.isNull(result) || !(result instanceof byte[]) || Objects.isNull(TraceContext.getTraceId())) {
            return result;
        }

        return this.injectTraceId(new String((byte[])result)).getBytes();
    }

    /**
     * Inject trace id into log message
     * @param message String
     * @return result
     */
    private String injectTraceId(String message) {
        try {
            final boolean newLine = message.endsWith("\n");

            String trimMessage = message.trim();
            final boolean jsonFormat = trimMessage.startsWith("{") && trimMessage.endsWith("}");

            if (jsonFormat) {
                return injectJsonFormat(message, trimMessage, newLine);
            } else {
                if (message.contains(TraceContext.getTraceId() + SEPARATOR)) {
                    return message;
                }

                String preTrimMessage = "[TraceId]" + TraceContext.getTraceId() + SEPARATOR;
                if (Objects.nonNull(TraceContext.getSpanId())) {
                    preTrimMessage += "[SpanId]" + TraceContext.getSpanId() + SEPARATOR;
                }
                if (Objects.nonNull(TraceContext.getParentSpanId())) {
                    preTrimMessage += "[P-SpanId]" + TraceContext.getParentSpanId() + SEPARATOR;
                }
                trimMessage = preTrimMessage + trimMessage;
            }
            return newLine ? trimMessage + "\n" : trimMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    private String injectJsonFormat(String message, String trimMessage, boolean newLine) {
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

        return newLine ? trimMessage + "\n" : trimMessage;
    }
}
