package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Dubbo Consumer增强转换器
 * <p>
 * @author orangewest
 * @since 2024-07-30
 */
@SuppressWarnings("unused")
public class DubboConsumerTransformer extends AbsDelegateTransformer.AbsInstance {

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
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
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
    protected void onMethodEnter(Object thiz, Object[] allArgs, Method originMethod) throws Exception {
        String traceId = TraceContext.getTraceId();
        if (Objects.nonNull(traceId)) {
            MethodWrapper methodWrapper = ReflectUtils.getMethod(allArgs[1]
                    , "setAttachment", String.class, String.class);
            methodWrapper.invoke(TraceContext.ATO_TRACE_ID, traceId);
            final String spanId = TraceContext.getSpanId();
            if (Objects.nonNull(spanId)) {
                methodWrapper.invoke(TraceContext.ATO_SPAN_ID, spanId);
            }
        }
    }

}
