package io.github.artlibs.autotrace4j.context.jdk;

import io.github.artlibs.autotrace4j.context.TraceContext;

import java.util.Objects;

/**
 * Thread Pool Task
 *
 * @author Fury
 * @since 2024-03-30
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
            TraceContext.setSpanId(TraceContext.generate());

            if (Objects.nonNull(traceId)) {
                TraceContext.setTraceId(traceId);
            }

            if (Objects.nonNull(spanId)) {
                TraceContext.setParentSpanId(spanId);
            }

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
