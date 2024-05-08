package io.github.artlibs.autotrace4j.interceptor.common;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 功能：AbstractTaskVisitorInterceptor
 *
 * @author suopovate
 * @since 2024/04/13
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractTaskVisitorInterceptor extends AbstractVisitorInterceptor implements TaskInterceptor {

    abstract protected ElementMatcher<? super MethodDescription> traceInjectedMethod();

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(Advice.to(FillTraceAdvisor.class).on(traceInjectedMethod()));
    }

    private static class FillTraceAdvisor {

        /**
         * advice on method enter
         */
        @Advice.OnMethodEnter
        private static void adviceOnMethodEnter(
            @Advice.FieldValue(value = AutoTraceCtx.TRACE_KEY, readOnly = false) String traceId,
            @Advice.FieldValue(value = AutoTraceCtx.SPAN_KEY, readOnly = false) String spanId
        ) {
            try {
                // setup defined field on method exit
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setParentSpanId(spanId);
                AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * advice on method exit: remove trace id
         */
        @Advice.OnMethodExit
        private static void adviceOnMethodExit() {
            AutoTraceCtx.removeAll();
        }

    }

}
