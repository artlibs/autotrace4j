package com.github.artlibs.autotrace4j.interceptor.common;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.ReflectUtils;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 功能：AbstractCallbackInstanceInterceptor
 *
 * @author Fury
 * @author suopovate
 * @since 2024/04/13
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractCallbackInstanceInterceptor extends AbstractInstanceInterceptor implements CallbackInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        String traceId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.TRACE_KEY);
        if (Objects.nonNull(traceId)) {
            AutoTraceCtx.setTraceId(traceId);
        }

        String spanId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.SPAN_KEY);
        if (Objects.nonNull(spanId)) {
            AutoTraceCtx.setSpanId(spanId);
        }

        String parentSpanId = ReflectUtils.getFieldValue(thiz, AutoTraceCtx.PARENT_SPAN_KEY);
        if (Objects.nonNull(parentSpanId)) {
            AutoTraceCtx.setParentSpanId(parentSpanId);
        }
    }

    @Override
    public Object onMethodExit(Object thiz, Object[] allArgs, Object result, Method originMethod) throws Exception {
        try {
            AutoTraceCtx.removeAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
