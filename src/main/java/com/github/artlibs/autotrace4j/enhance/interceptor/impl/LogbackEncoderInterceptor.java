package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.enhance.interceptor.Instance;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 功能：增强 Logback 输出日志, 将 TraceId 插入到输出日志消息中
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class LogbackEncoderInterceptor extends Instance {
    private static final String SEPARATOR = " - ";
    private static final String QUOTE_COLON = "\":\"";
    private static final String LAYOUT_WRAPPING_ENCODER
            = "ch.qos.logback.core.encoder.LayoutWrappingEncoder";

    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.hasSuperType(named(LAYOUT_WRAPPING_ENCODER));
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("encode").and(
                ElementMatchers.isOverriddenFrom(named(LAYOUT_WRAPPING_ENCODER)).or(
                ElementMatchers.isDeclaredBy(named(LAYOUT_WRAPPING_ENCODER)) )
        );
    }

    /**
     * 在原方法返回前执行
     * @param thiz 增强的对象实例
     * @param allArgs 原方法的参数表
     * @param result 方法执行结果
     * @param originMethod 原方法
     * @return Object - result
     * @throws Exception -
     */
    @Override
    public Object afterMethod(Object thiz, Object[] allArgs, Object result, Method originMethod) throws Exception {
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
    protected String injectTraceId(String message) {
        try {
            final boolean newLine = message.endsWith("\n");

            String trimMessage = message.trim();
            final boolean jsonFormat = trimMessage.startsWith("{") && trimMessage.endsWith("}");

            if (jsonFormat) {
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
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return message;
    }
}
