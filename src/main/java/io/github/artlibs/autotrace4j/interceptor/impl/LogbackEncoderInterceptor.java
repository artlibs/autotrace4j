package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Logback Encoder Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class LogbackEncoderInterceptor extends AbstractInstanceInterceptor {
    private static final String SEPARATOR = " - ";
    private static final String QUOTE_COLON = "\":\"";
    private static final String LAYOUT_WRAPPING_ENCODER
            = "ch.qos.logback.core.encoder.LayoutWrappingEncoder";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(named(LAYOUT_WRAPPING_ENCODER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("encode").and(
                ElementMatchers.isOverriddenFrom(named(LAYOUT_WRAPPING_ENCODER)).or(
                ElementMatchers.isDeclaredBy(named(LAYOUT_WRAPPING_ENCODER)) )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) {
        if (Objects.isNull(result) || !(result instanceof byte[]) || Objects.isNull(AutoTraceCtx.getTraceId())) {
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
                if (message.contains(AutoTraceCtx.getTraceId() + SEPARATOR)) {
                    return message;
                }

                String preTrimMessage = AutoTraceCtx.getTraceId() + SEPARATOR;
                if (Objects.nonNull(AutoTraceCtx.getSpanId())) {
                    preTrimMessage += AutoTraceCtx.getSpanId() + SEPARATOR;
                }
                if (Objects.nonNull(AutoTraceCtx.getParentSpanId())) {
                    preTrimMessage += AutoTraceCtx.getParentSpanId() + SEPARATOR;
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
        if (message.contains(AutoTraceCtx.ATO_TRACE_ID) && message.contains(AutoTraceCtx.getTraceId())) {
            return message;
        }

        String injectedTraceFields = "\"" + AutoTraceCtx.ATO_TRACE_ID + QUOTE_COLON + AutoTraceCtx.getTraceId() + "\",";
        if (Objects.nonNull(AutoTraceCtx.getSpanId())) {
            injectedTraceFields = injectedTraceFields
                    + "\"" + AutoTraceCtx.ATO_SPAN_ID + QUOTE_COLON + AutoTraceCtx.getSpanId() + "\",";
        }
        if (Objects.nonNull(AutoTraceCtx.getParentSpanId())) {
            injectedTraceFields = injectedTraceFields
                    + "\"" + AutoTraceCtx.ATO_PARENT_SPAN_ID + QUOTE_COLON + AutoTraceCtx.getParentSpanId() + "\",";
        }
        trimMessage = "{" + injectedTraceFields + trimMessage.substring(1);

        return newLine ? trimMessage + "\n" : trimMessage;
    }
}
