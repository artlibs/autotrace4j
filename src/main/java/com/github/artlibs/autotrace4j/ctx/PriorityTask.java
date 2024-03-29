package com.github.artlibs.autotrace4j.ctx;

/**
 * Priority Task
 *
 * @author Fury
 * @since 2023-01-03
 *
 * All rights Reserved.
 */
public class PriorityTask extends ThreadPoolTask implements Comparable<PriorityTask> {
    public PriorityTask(Runnable rawTask, String traceId, String spanId) {
        super(rawTask, traceId, spanId);
    }

    @SuppressWarnings("unchecked")
    public Comparable<PriorityTask> getComparableRawTask() {
        return (Comparable<PriorityTask>)this.getRawTask();
    }

    @Override
    public int compareTo(PriorityTask task) {
        return this.getComparableRawTask().compareTo((PriorityTask)task.getComparableRawTask());
    }
}
