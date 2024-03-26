package com.github.artlibs.autotrace4j.enhance.interceptor.impl;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.ctx.PriorityTask;
import com.github.artlibs.autotrace4j.ctx.ThreadPoolTask;
import com.github.artlibs.autotrace4j.enhance.interceptor.Visitor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.util.Objects;

/**
 * 功能：增强 ThreadPoolExecutor 线程池
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class ThreadPoolExecutorInterceptor extends Visitor {
    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.is(java.util.concurrent.ThreadPoolExecutor.class);
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.named("execute")
                .and(ElementMatchers.takesArgument(0, Runnable.class));
    }

    /**
     * 指明Visitor类，自己实现Visitor代码
     *
     * @return visitor Class
     */
    @Override
    public Class<?> visitor() {
        return Visitor.class;
    }

    public static class Visitor {
        private Visitor() {}

        @Advice.OnMethodEnter
        public static void intercept(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) Runnable task) throws Exception {
            try {
                if (Objects.nonNull(task)) {
                    String traceId = AutoTraceCtx.getTraceId();
                    if (Objects.nonNull(traceId) && !(task instanceof ThreadPoolTask)) {
                        if (task instanceof Comparable) {
                            task = new PriorityTask(task, traceId, AutoTraceCtx.getSpanId());
                        } else {
                            task = new ThreadPoolTask(task, traceId, AutoTraceCtx.getSpanId());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
