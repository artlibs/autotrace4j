package com.github.artlibs.autotrace4j.interceptor.common;

import com.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.ReflectUtils;
import com.github.artlibs.autotrace4j.support.Constants;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Abstract Servlet Interceptor
 *
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractServletInterceptor extends AbstractInstanceInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        Class<?> httpReqClazz, httpRespClazz;
        try {
            httpReqClazz = Class.forName(
                "javax.servlet.http.HttpServletRequest", false, AbstractServletInterceptor.class.getClassLoader()
            );
            httpRespClazz = Class.forName(
                "javax.servlet.http.HttpServletResponse", false, AbstractServletInterceptor.class.getClassLoader()
            );
        } catch (ClassNotFoundException e) {
            // warning that we can't intercept the servlet
            return;
        }
        Class<?> argServletReqClazz = allArgs[0].getClass(), argServletRespClazz = allArgs[1].getClass();
        boolean isHttp = httpReqClazz.isAssignableFrom(argServletReqClazz) && httpRespClazz.isAssignableFrom(argServletRespClazz);
        // just intercept http request
        if (isHttp) {
            // first we take it from the req attributes
            String traceId = ReflectUtils
                .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, Object.class)
                .invoke(AutoTraceCtx.ATO_TRACE_ID);
            String parentSpanId = ReflectUtils
                .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, Object.class)
                .invoke(AutoTraceCtx.ATO_SPAN_ID);
            if (Objects.nonNull(traceId)) {
                ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                    .invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
                ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                    .invoke(AutoTraceCtx.ATO_SPAN_ID, parentSpanId);
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setParentSpanId(parentSpanId);
            } else {
                traceId = ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                    .invoke(AutoTraceCtx.ATO_TRACE_ID);
                parentSpanId = ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                    .invoke(AutoTraceCtx.ATO_SPAN_ID);

                boolean setTraceIdToResponse = true;
                if (Objects.isNull(traceId)) {
                    traceId = ReflectUtils
                        .getMethodWrapper(allArgs[1], Constants.GET_HEADER, String.class)
                        .invoke(AutoTraceCtx.ATO_TRACE_ID);
                    parentSpanId = ReflectUtils
                        .getMethodWrapper(allArgs[1], Constants.GET_HEADER, String.class)
                        .invoke(AutoTraceCtx.ATO_SPAN_ID);

                    setTraceIdToResponse = Objects.isNull(traceId);
                }

                if (Objects.isNull(traceId)) {
                    traceId = AutoTraceCtx.generate();
                }
                final String spanId = AutoTraceCtx.generate();

                AutoTraceCtx.setSpanId(spanId);
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setParentSpanId(parentSpanId);

                if (setTraceIdToResponse) {
                    ReflectUtils
                        .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                        .invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
                    ReflectUtils
                        .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                        .invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
                }
            }
        }
    }

}
