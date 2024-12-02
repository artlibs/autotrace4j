package io.github.artlibs.autotrace4j.transformer.impl.jdk;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.context.jdk.ThreadPoolTask;
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
                             takesArgument(5, boolean.class),
                             returns(void.class)
                         )
                ))
        );
    }

    public static class AdviceConstructor {
        private AdviceConstructor() {}

        /**
         * OnMethodEnter
         * @param runnable -
         */
        @Advice.OnMethodEnter
        private static void adviceOnMethodEnter(
            @Advice.Argument(value = 3, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Runnable runnable) {
            runnable = wrappedRunnable(runnable);
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
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) Runnable runnable) {
            runnable = wrappedRunnable(runnable);
        }
    }

    private static Runnable wrappedRunnable(Runnable runnable) {
        try {
            if (Objects.nonNull(runnable)) {
                String traceId = TraceContext.getTraceId();
                if (Objects.nonNull(traceId) && !(runnable instanceof ThreadPoolTask)) {
                    return new ThreadPoolTask(runnable, traceId, TraceContext.getSpanId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return runnable;
    }
}
