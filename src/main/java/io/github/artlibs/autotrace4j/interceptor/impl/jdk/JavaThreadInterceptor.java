package io.github.artlibs.autotrace4j.interceptor.impl.jdk;

import io.github.artlibs.autotrace4j.context.AutoTraceCtx;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
import io.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
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

    public static final String RUNNABLE_CLS = "java.lang.Runnable";
    public static final String THREAD_GROUP_CLS = "java.lang.ThreadGroup";
    public static final String STRING_CLS = "java.lang.String";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return named("java.lang.Thread");
    }

    /**
     * jdk21: Thread(ThreadGroup g, String name, int characteristics, Runnable task, long stackSize, AccessControlContext acc)
     * jdk1.8: private void init(ThreadGroup g, Runnable target, String name, long stackSize, AccessControlContext acc, boolean inheritThreadLocals)
     * {@inheritDoc}
     */
    @Override
    public Map<Class<?>, ElementMatcher<? super MethodDescription>> methodMatchers() {
        return newMmHolder()
            .put(AdviceConstructor.class, isConstructor().and(isPackagePrivate())
                .and(takesArgument(3, named(RUNNABLE_CLS)))
            ).put(AdviceInit.class, named("init").and(isPrivate())
                .and(new ElementMatcher.Junction.Conjunction<>(
                         Arrays.asList(
                             takesArgument(0, named(THREAD_GROUP_CLS)),
                             takesArgument(1, named(RUNNABLE_CLS)),
                             takesArgument(2, named(STRING_CLS)),
                             takesArgument(3, long.class),
                             takesArgument(4, named("java.security.AccessControlContext")),
                             takesArgument(5, boolean.class),
                             returns(void.class)
                         )
                     )
                )).get();
    }

    public static class AdviceConstructor {
        private AdviceConstructor() {}

        /**
         * 注：各Advice的代码相同, 不能去冗余
         *
         * @param runnable -
         */
        @Advice.OnMethodEnter
        private static void adviceOnMethodEnter(
            @Advice.Argument(value = 3
                , typing = Assigner.Typing.DYNAMIC, optional = false
                , readOnly = false) Runnable runnable
        ) {
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

    public static class AdviceInit {
        private AdviceInit() {}

        /**
         * 注：各Advice的代码相同, 不能去冗余
         *
         * @param runnable -
         */
        @Advice.OnMethodEnter
        private static void adviceOnMethodEnter(
            @Advice.Argument(value = 1
                , typing = Assigner.Typing.DYNAMIC, optional = false
                , readOnly = false) Runnable runnable
        ) {
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
