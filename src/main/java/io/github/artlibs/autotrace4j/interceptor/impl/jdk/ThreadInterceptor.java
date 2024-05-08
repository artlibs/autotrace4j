package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 功能：ForkJoinTaskInterceptor
 *
 * @author suopovate
 * @since 2024-04-13
 * <p>
 * All rights Reserved.
 */
public class ThreadInterceptor extends AbstractVisitorInterceptor {

    @Override
    public DynamicType.Builder<?> visit(DynamicType.Builder<?> builder) {
        return builder.visit(
            Advice
                .to(Constructor0Advisor.class)
                .on(isConstructor()
                        .and(isPublic())
                        .and(takesArgument(0, Runnable.class))
                )
        ).visit(
            Advice
                .to(Constructor1Advisor.class)
                .on(isConstructor()
                        .and(isPublic())
                        .and(takesArgument(1, Runnable.class))
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.named("java.lang.Thread");
    }

    public static class Constructor0Advisor {

        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.Argument(
                value = 0, typing = Assigner.Typing.DYNAMIC, optional = false, readOnly = false
            ) Runnable runnable
        ) throws Exception {
            try {
                if (Objects.nonNull(runnable)) {
                    String traceId = AutoTraceCtx.getTraceId();
                    if (Objects.nonNull(traceId) && !(runnable instanceof ThreadPoolTask)) {
                        runnable = new ThreadPoolTask(runnable, traceId, AutoTraceCtx.getSpanId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Constructor1Advisor {

        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.Argument(
                value = 1, typing = Assigner.Typing.DYNAMIC, optional = false, readOnly = false
            ) Runnable runnable
        ) throws Exception {
            try {
                if (Objects.nonNull(runnable)) {
                    String traceId = AutoTraceCtx.getTraceId();
                    if (Objects.nonNull(traceId) && !(runnable instanceof ThreadPoolTask)) {
                        runnable = new ThreadPoolTask(runnable, traceId, AutoTraceCtx.getSpanId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
