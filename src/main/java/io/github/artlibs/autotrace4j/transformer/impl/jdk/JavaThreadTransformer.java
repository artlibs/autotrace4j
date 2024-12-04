package io.github.artlibs.autotrace4j.transformer.impl.jdk;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.jdk.ThreadTask;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Thread 增强转换器
 *      上下文有Trace ID时传递
 * <p>
 * @author suopovate
 * @since 2024-04-13
 * <p>
 * All rights Reserved.
 */
public class JavaThreadTransformer extends AbsVisitorTransformer {
    private static final String RUNNABLE_CLS = "java.lang.Runnable";
    private static final String THREAD_GROUP_CLS = "java.lang.ThreadGroup";
    private static final String STRING_CLS = "java.lang.String";

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
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(AdviceConstructor.class, isConstructor()
                .and(isPackagePrivate())
                .and(takesArgument(3, named(RUNNABLE_CLS)))
        ).ofMatcher(AdviceInit.class, named("init").and(isPrivate())
                .and(new ElementMatcher.Junction.Conjunction<>(
                         Arrays.asList(
                             takesArgument(0, named(THREAD_GROUP_CLS)),
                             takesArgument(1, named(RUNNABLE_CLS)),
                             takesArgument(2, named(STRING_CLS)),
                             takesArgument(3, long.class),
                             takesArgument(4, named("java.security.AccessControlContext")),
                            // takesArgument(5, boolean.class),
                             returns(void.class)
                         )
                ))
        );
    }

    public static class AdviceConstructor {
        private AdviceConstructor() {}

        /**
         * 里面调用的其他方法需要在运行时能访问到
         * @param runnable -
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.Argument(value = 3, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Runnable runnable) {
            // Wrap only when there is trace info in the context
            String traceId = TraceContext.getTraceId();
            if (Objects.nonNull(traceId) && Objects.nonNull(runnable) && !(runnable instanceof ThreadTask)) {
                runnable = new ThreadTask(runnable, traceId, TraceContext.getSpanId());
            }
        }
    }

    public static class AdviceInit {
        private AdviceInit() {}

        /**
         * 里面调用的其他方法需要在运行时能访问到
         * @param runnable -
         */
        @Advice.OnMethodEnter
        public static void adviceOnMethodEnter(
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Runnable runnable) {
            // Wrap only when there is trace info in the context
            String traceId = TraceContext.getTraceId();
            if (Objects.isNull(traceId) || Objects.isNull(runnable) || runnable instanceof ThreadTask) {
                return;
            }
            runnable = new ThreadTask(runnable, traceId, TraceContext.getSpanId());
        }
    }
}
