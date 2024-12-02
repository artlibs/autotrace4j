package io.github.artlibs.autotrace4j.context.jdk;

import io.github.artlibs.autotrace4j.context.TraceContext;

import java.util.Objects;

/**
 * Thread Pool Task
 *      执行一个任务时如果存在Trace ID传递则取出设置，否则生成新的Trace ID
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
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
     * transfer the spanId and traceId to the next context
     * @param rawTask The original runnable task
     * @param traceId traceId for binding to this task
     * @param spanId current context spanId，as the parent span id of the next context
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
            if (Objects.nonNull(traceId)) {
                TraceContext.setTraceId(traceId);
            } else {
                TraceContext.setTraceId(TraceContext.generate());
            }
            if (Objects.nonNull(spanId)) {
                TraceContext.setParentSpanId(spanId);
            }

            // Always start a new span with a new span id
            TraceContext.setSpanId(TraceContext.generate());

            this.rawTask.run();
        } finally {
            TraceContext.removeAll();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.rawTask.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.rawTask.hashCode();
    }
}
