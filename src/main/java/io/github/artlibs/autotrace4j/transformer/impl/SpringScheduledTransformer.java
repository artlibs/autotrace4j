package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Spring Task @Scheduled 增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class SpringScheduledTransformer extends AbsDelegateTransformer.AbsInstance {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return declaresMethod(isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ElementMatcher<? super MethodDescription> methodMatcher() {
        return isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMethodEnter(Object obj, Object[] allArgs, Method originMethod) throws Exception {
        TraceContext.setSpanId(TraceContext.generate());
        TraceContext.setTraceId(TraceContext.generate());
        // There will be no parent span as this is a startup context
        TraceContext.setParentSpanId(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object onMethodExit(Object obj, Object[] allArgs, Object result, Method originMethod) throws Exception {
        TraceContext.removeAll();
        return result;
    }

}
