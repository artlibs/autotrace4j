package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.core.interceptor.AbstractInstance;
import com.github.artlibs.autotrace4j.ctx.ReflectUtils;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * OkHttp Client Interceptor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class OkHttpClientInterceptor extends AbstractInstance {
    /**
     * 在原方法刚开始进入时执行
     * @param thiz 增强的对象实例
     * @param allArgs 原方法的参数表
     * @param originMethod 原方法
     * @throws Exception -
     */
    @Override
    public void beforeMethod(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        final String traceId = AutoTraceCtx.getTraceId();
        if (Objects.isNull(traceId) || Objects.isNull(thiz)) {
            return;
        }
        List<String> namesAndValues = ReflectUtils.getFieldValue(thiz, "namesAndValues");
        if (Objects.nonNull(namesAndValues) && !namesAndValues.contains(AutoTraceCtx.ATO_TRACE_ID)) {
            namesAndValues.add(AutoTraceCtx.ATO_TRACE_ID);
            namesAndValues.add(traceId);

            namesAndValues.add(AutoTraceCtx.TRACE_HEADER);
            namesAndValues.add(traceId);

            final String spanId = AutoTraceCtx.getSpanId();
            if (Objects.nonNull(spanId)) {
                namesAndValues.add(AutoTraceCtx.ATO_SPAN_ID);
                namesAndValues.add(spanId);
            }
        }
    }


    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("okhttp3.Headers$Builder");
    }

    /**
     * okhttp3.Request.Builder.build()
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("build");
    }
}
