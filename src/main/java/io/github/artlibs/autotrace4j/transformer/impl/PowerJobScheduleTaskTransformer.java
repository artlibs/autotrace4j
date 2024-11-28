package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.transformer.abs.AbsDelegateTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * PowerJob Interceptor
 *
 * @author orangewest
 * @since 2024-07-30
 * <p>
 */
public class PowerJobScheduleTaskTransformer extends AbsDelegateTransformer.Instance {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("tech.powerjob.worker.core.processor.sdk.BasicProcessor"))
                .and(not(isInterface())).and(not(isAbstract()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return named("process")
                .and(takesArgument(0, named("tech.powerjob.worker.core.processor.TaskContext")));
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
