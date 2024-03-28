package com.github.artlibs.autotrace4j.ctx;

import java.util.Objects;

/**
 * 功能：JDK线程池任务包装器
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public class ThreadPoolTask implements Runnable {
    private String spanId;
    private String traceId;
    private Runnable rawTask;
    private long callerThreadId;

    private ThreadPoolTask() {}

    public Runnable getRawTask() {
        return this.rawTask;
    }

    /**
     * 把 trace id 和 当前上下文的spanId下传
     * @param rawTask The original runnable task
     * @param traceId Trace id for binding to this task
     * @param spanId 当前上下文的spanId，传递到下游作为下游的parent span id
     */
    public ThreadPoolTask(Runnable rawTask, String traceId, String spanId) {
        this.spanId = spanId;
        this.traceId = traceId;
        this.rawTask = rawTask;
        this.callerThreadId = Thread.currentThread().getId();
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (Objects.isNull(this.rawTask)) {
            return;
        }

        if (this.callerThreadId == Thread.currentThread().getId()) {
            this.rawTask.run();
            return;
        }

        try {
            AutoTraceCtx.setSpanId(AutoTraceCtx.generate());

            if (Objects.nonNull(traceId)) {
                AutoTraceCtx.setTraceId(traceId);
            }

            if (Objects.nonNull(spanId)) {
                AutoTraceCtx.setParentSpanId(spanId);
            }

            this.rawTask.run();
        } finally {
            AutoTraceCtx.removeAll();
        }
    }
}
