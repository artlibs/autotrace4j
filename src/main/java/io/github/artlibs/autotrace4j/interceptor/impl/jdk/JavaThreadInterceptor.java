package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 功能：JdkThreadInterceptor
 *
 * @author suopovate
 * @since 2024-04-13
 * <p>
 * All rights Reserved.
 */
public class JavaThreadInterceptor extends AbstractVisitorInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.lang.Thread");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
        return newMmHolder()
                .put(Advice0.class, isConstructor().and(isPublic())
                        .and(takesArgument(0, Runnable.class))
                ).put(Advice1.class, isConstructor().and(isPublic())
                        .and(takesArgument(1, Runnable.class))
                ).get();
    }

    public static class Advice0 {
        private Advice0(){}

        /**
         * 注：与Advice1的代码相同, 不能去冗余
         * @param runnable -
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(@Advice.Argument(value = 0
                , typing = Assigner.Typing.DYNAMIC, optional = false
                , readOnly = false) Runnable runnable) {
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

    public static class Advice1 {
        private Advice1(){}

        /**
         * 注：与Advice0的代码相同, 不能去冗余
         * @param runnable -
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(@Advice.Argument(value = 1
                , typing = Assigner.Typing.DYNAMIC, optional = false
                , readOnly = false) Runnable runnable) {
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
