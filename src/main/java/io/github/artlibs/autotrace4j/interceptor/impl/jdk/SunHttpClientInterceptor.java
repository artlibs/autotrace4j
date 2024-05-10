package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.MethodWrapper;
import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Sun HttpClient Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class SunHttpClientInterceptor extends AbstractVisitorInterceptor {
    private static final String SET_IF_NOT_SET = "setIfNotSet";
    private static final String WRITE_REQUESTS = "writeRequests";
    private static final String POS_CLASS = "sun.net.www.http.PosterOutputStream";
    private static final String MESSAGE_HEADER_CLS = "sun.net.www.MessageHeader";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("sun.net.www.http.HttpClient");
    }

    @Override
    public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
        return ofMatcher(named(WRITE_REQUESTS)
                .and(takesArgument(0, named(MESSAGE_HEADER_CLS)))
                .or(
                        named(WRITE_REQUESTS)
                                .and(takesArgument(0, named(MESSAGE_HEADER_CLS)))
                                .and(takesArgument(1, named(POS_CLASS)))
                )
                .or(
                        named(WRITE_REQUESTS)
                                .and(takesArgument(0, named(MESSAGE_HEADER_CLS)))
                                .and(takesArgument(1, named(POS_CLASS)))
                                .and(takesArgument(2, boolean.class))
                )
        );
    }

    /**
     * advice on method enter: set http header
     *
     * @param msgHeader http message header
     */
    @Advice.OnMethodEnter
    private static void adviceOnMethodEnter(
        @Advice.Argument(value = 0, readOnly = false
            , typing = Assigner.Typing.DYNAMIC) Object msgHeader
    ) {
        try {
            final String traceId = AutoTraceCtx.getTraceId();
            if (Objects.nonNull(traceId)) {
                MethodWrapper methodWrapper = ReflectUtils.getMethodWrapper(msgHeader
                    , SET_IF_NOT_SET, String.class, String.class);
                methodWrapper.invoke(AutoTraceCtx.ATO_TRACE_ID, traceId);

                final String spanId = AutoTraceCtx.getSpanId();
                if (Objects.nonNull(spanId)) {
                    methodWrapper.invoke(AutoTraceCtx.ATO_SPAN_ID, spanId);
                }
            }
        } catch (Exception ignore) {
            // No sonar
        }
    }

}
