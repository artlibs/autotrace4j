package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.support.Constants;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * ApacheHttpClient增强转换器：
 *      发出请求时，如果当前Thread上下文存在trace id则将其设置到请求头当中进行传递
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class ApacheHttpClientTransformer extends AbsDelegateTransformer.AbsInstance {

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
            MethodWrapper setHeaderMethod = ReflectUtils.getMethod(allArgs[1]
                    , Constants.SET_HEADER, String.class, String.class);
            setHeaderMethod.invoke(TraceContext.ATO_TRACE_ID, traceId);
            final String spanId = TraceContext.getSpanId();
            if (Objects.nonNull(spanId)) {
                setHeaderMethod.invoke(TraceContext.ATO_SPAN_ID, spanId);
            }
        }
    }
}
