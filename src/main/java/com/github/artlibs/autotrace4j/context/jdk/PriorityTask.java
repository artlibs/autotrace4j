package com.github.artlibs.autotrace4j.context.jdk;

/**
 * Priority Task
 *
 * @author Fury
 * @since 2024-03-30
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

    @Override
    public boolean equals(Object obj) {
        return this.getRawTask().equals(obj);
    }

    @Override
    public int hashCode() {
        return this.getRawTask().hashCode();
    }
}
