package com.github.artlibs.autotrace4j.core.interceptor.impl;

import com.github.artlibs.autotrace4j.core.interceptor.AbstractVisitorInterceptor;
import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;
import com.github.artlibs.autotrace4j.ctx.ScheduledTask;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.util.Objects;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * Scheduled ThreadPoolExecutor
 *
 * @author Fury
 * @since 2023-01-04
 *
 * All rights Reserved.
 */
public class ScheduledThreadPoolInterceptor extends AbstractVisitorInterceptor {
    /**
     * 类型匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return ElementMatchers.is(java.util.concurrent.ScheduledThreadPoolExecutor.class);
    }

    /**
     * 方法匹配器
     *
     * @return ElementMatcher
     */
    @Override
    public ElementMatcher<? super MethodDescription> methodMatcher() {
        return ElementMatchers.isPrivate().and(ElementMatchers.named("delayedExecute"))
                .and(ElementMatchers.takesArgument(0, RunnableScheduledFuture.class));
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
        public static void intercept(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) RunnableScheduledFuture<?> task) throws Exception {
            // 检测当前运行环境，如果是jar，就先将ctx包注入到
            try {
                if (Objects.nonNull(task)) {
                    String traceId = AutoTraceCtx.getTraceId();
                    if (Objects.nonNull(traceId) && !(task instanceof ScheduledTask)) {
                        task = new ScheduledTask<>(task, traceId, AutoTraceCtx.getSpanId());
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
