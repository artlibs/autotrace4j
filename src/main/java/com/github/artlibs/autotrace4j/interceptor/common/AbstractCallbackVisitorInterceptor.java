package com.github.artlibs.autotrace4j.interceptor.common;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 功能：AbstractCallbackVisitorInterceptor
 *
 * @author Fury
 * @author suopovate
 * @since 2024/04/13
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractCallbackVisitorInterceptor extends AbstractVisitorInterceptor implements CallbackInterceptor {

    abstract protected ElementMatcher<? super MethodDescription> traceInjectedMethod();

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(Advice.to(FillTraceAdvisor.class).on(traceInjectedMethod()));
    }

    public static class FillTraceAdvisor {

        /**
         * advice on method enter
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.FieldValue(value = AutoTraceCtx.TRACE_KEY, readOnly = false) String traceId,
            @Advice.FieldValue(value = AutoTraceCtx.SPAN_KEY, readOnly = false) String spanId,
            @Advice.FieldValue(value = AutoTraceCtx.PARENT_SPAN_KEY, readOnly = false) String parentSpanId
        ) {
            try {
                // setup defined field on method exit
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setSpanId(spanId);
                AutoTraceCtx.setParentSpanId(parentSpanId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * advice on method exit: remove trace id
         */
        @Advice.OnMethodExit
        public static void adviceOnMethodExit() {
            AutoTraceCtx.removeAll();
        }

    }

}
