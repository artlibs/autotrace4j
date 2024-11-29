package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import io.github.artlibs.autotrace4j.support.Constants;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * ApacheHttpClient增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class ApacheHttpClientTransformer extends AbsDelegateTransformer.Instance {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.client.AbstractHttpClient")
                .or(named("org.apache.http.impl.client.MinimalHttpClient"))
                .or(named("org.apache.http.impl.client.InternalHttpClient"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("doExecute");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        final String traceId = TraceContext.getTraceId();
        if (Objects.nonNull(traceId)) {
            MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[1]
                    , Constants.SET_HEADER, String.class, String.class);

            methodWrapper.invoke(TraceContext.ATO_TRACE_ID, traceId);
            final String spanId = TraceContext.getSpanId();
            if (Objects.nonNull(spanId)) {
                methodWrapper.invoke(TraceContext.ATO_SPAN_ID, spanId);
            }
        }
    }
}
