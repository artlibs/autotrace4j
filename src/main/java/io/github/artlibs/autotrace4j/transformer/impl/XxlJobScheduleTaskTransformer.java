package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Xxl Job Interceptor
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class XxlJobScheduleTaskTransformer extends AbsDelegateTransformer.Instance {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return bizScopeJunction().and(
                hasSuperClass(named("com.xxl.job.core.handler.IJobHandler"))
                // Or has functions annotated with @XxlJob
                .or(declaresMethod(isAnnotatedWith(named(("com.xxl.job.core.handler.annotation.XxlJob")))))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return isAnnotatedWith(named("com.xxl.job.core.handler.annotation.XxlJob"))
                .or(named("execute").and(takesNoArguments()))
                .or(named("execute").and(takesArgument(0, String.class)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter(Object obj, Object[] allArgs, Method originMethod) throws Exception {
        TraceContext.setTraceId(TraceContext.generate());
        TraceContext.setSpanId(TraceContext.generate());
        // There will be no parent span as this is a startup context
        TraceContext.setParentSpanId(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onMethodExit(Object obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        TraceContext.removeAll();
        return result;
    }

}
