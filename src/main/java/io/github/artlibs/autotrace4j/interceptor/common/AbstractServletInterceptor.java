package io.github.artlibs.autotrace4j.interceptor.common;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import io.github.artlibs.autotrace4j.support.Constants;

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
                .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                .invoke(AutoTraceCtx.ATO_TRACE_ID);
            String parentSpanId = ReflectUtils
                .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                .invoke(AutoTraceCtx.ATO_PARENT_SPAN_ID);
            String spanId = ReflectUtils
                .getMethodWrapper(allArgs[0], Constants.GET_ATTRIBUTE, String.class)
                .invoke(AutoTraceCtx.ATO_SPAN_ID);

            if (Objects.nonNull(traceId)) {
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setParentSpanId(parentSpanId);
                AutoTraceCtx.setSpanId(spanId);
            } else {
                traceId = ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                    .invoke(AutoTraceCtx.ATO_TRACE_ID);
                parentSpanId = ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.GET_HEADER, String.class)
                    .invoke(AutoTraceCtx.ATO_SPAN_ID);

                if (Objects.isNull(traceId)) {
                    traceId = AutoTraceCtx.generate();
                }
                spanId = AutoTraceCtx.generate();

                AutoTraceCtx.setSpanId(spanId);
                AutoTraceCtx.setTraceId(traceId);
                AutoTraceCtx.setParentSpanId(parentSpanId);

                ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                    .invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
                ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                    .invoke(AutoTraceCtx.ATO_PARENT_SPAN_ID, parentSpanId);
                ReflectUtils
                    .getMethodWrapper(allArgs[0], Constants.SET_ATTRIBUTE, String.class, Object.class)
                    .invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
                ReflectUtils
                    .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                    .invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
                ReflectUtils
                    .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                    .invoke(AutoTraceCtx.ATO_PARENT_SPAN_ID, parentSpanId);
                ReflectUtils
                    .getMethodWrapper(allArgs[1], Constants.SET_HEADER, String.class, String.class)
                    .invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
            }
        }
    }

}
