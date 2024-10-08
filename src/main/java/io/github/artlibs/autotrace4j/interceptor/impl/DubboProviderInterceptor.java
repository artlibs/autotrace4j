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
 * Dubbo Provider Interceptor
 *
 * @author orangewest
 * @since 2024-07-30
 */
public class DubboProviderInterceptor extends AbstractInstanceInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("org.apache.dubbo.rpc.protocol.dubbo.filter.TraceFilter");
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
        MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(allArgs[1]
                , "getAttachment", String.class);
        final String traceId = methodWrapper.invoke(AutoTraceCtx.ATO_TRACE_ID);
        if (Objects.nonNull(traceId)) {
            AutoTraceCtx.setTraceId(traceId);
            AutoTraceCtx.setParentSpanId(methodWrapper.invoke(AutoTraceCtx.ATO_SPAN_ID));
            AutoTraceCtx.setSpanId(AutoTraceCtx.generate());
        }
    }

}
