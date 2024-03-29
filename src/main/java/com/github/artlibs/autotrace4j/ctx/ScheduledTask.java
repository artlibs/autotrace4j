package com.github.artlibs.autotrace4j.ctx;

import java.util.concurrent.*;

/**
 * Scheduled Task
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public class ScheduledTask<T> extends ThreadPoolTask implements RunnableScheduledFuture<T> {
    private final RunnableScheduledFuture<T> rawTask;

    public ScheduledTask(RunnableScheduledFuture<T> rawTask, String traceId, String spanId) {
        super(rawTask, traceId, spanId);
        this.rawTask = rawTask;
    }

    @Override
    public boolean isPeriodic() {
        return this.rawTask.isPeriodic();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return this.rawTask.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed delayed) {
        return this.rawTask.compareTo(delayed);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.rawTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.rawTask.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.rawTask.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return this.rawTask.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.rawTask.get(timeout, unit);
    }
}
