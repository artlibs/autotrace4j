package io.github.artlibs.autotrace4j.interceptor.impl;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractInstanceInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Dubbo Consumer Interceptor
 *
 * @author orangewest
 * @since 2024-07-30
 */
public class DubboConsumerInterceptor extends AbstractInstanceInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.dubbo.rpc.protocol.dubbo.filter.FutureFilter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isPublic()
                .and(named("invoke")
                        .and(takesArgument(0, named("org.apache.dubbo.rpc.Invoker")))
                        .and(takesArgument(1, named("org.apache.dubbo.rpc.Invocation")))
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        String traceId = AutoTraceCtx.getTraceId();
        if (Objects.nonNull(traceId)) {
            MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[1]
                    , "setAttachment", String.class, String.class);
            methodWrapper.invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);
            final String spanId = AutoTraceCtx.getSpanId();
            if (Objects.nonNull(spanId)) {
                methodWrapper.invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
            }
        }
    }

}
