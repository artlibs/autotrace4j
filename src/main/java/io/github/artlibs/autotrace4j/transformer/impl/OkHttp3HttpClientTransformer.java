package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * OkHttp Client增强转换器
 *      发出请求时，如果当前Thread上下文存在trace id则将其设置到请求头当中进行传递
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class OkHttp3HttpClientTransformer extends AbsDelegateTransformer.AbsInstance {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("okhttp3.Headers$Builder");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("build");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) {
        final String traceId = TraceContext.getTraceId();
        if (Objects.isNull(traceId) || Objects.isNull(thiz)) {
            return;
        }
        List<String> namesAndValues = ReflectUtils.getDeclaredFieldValue(thiz, "namesAndValues");
        if (Objects.nonNull(namesAndValues) && !namesAndValues.contains(TraceContext.ATO_TRACE_ID)) {
            namesAndValues.add(TraceContext.ATO_TRACE_ID);
            namesAndValues.add(traceId);

            final String spanId = TraceContext.getSpanId();
            if (Objects.nonNull(spanId)) {
                namesAndValues.add(TraceContext.ATO_SPAN_ID);
                namesAndValues.add(spanId);
            }
        }
    }

}
