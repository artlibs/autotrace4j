package com.github.artlibs.autotrace4j.context.jdk;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.github.artlibs.autotrace4j.context.MethodWrapper;
import com.github.artlibs.autotrace4j.context.ReflectUtils;

import java.util.Objects;
import java.util.concurrent.ForkJoinTask;

/**
 * Fork Join Task
 * <p>
 * WrapForkTask is actually half a proxy object of the raw ForkJoinTask<V>
 * object, because some methods of ForkJoinTask<V> are called in the
 * WrapForkTask instance rather than in the Raw ForkJoinTask<V> instance.
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class WrapForkTask<V> extends ForkJoinTask<V> {
    private String spanId;
    private String traceId;
    private ForkJoinTask<V> rawTask;
    private long callerThreadId;

    protected WrapForkTask(){
        super();
    }

    public WrapForkTask(ForkJoinTask<V> task, String traceId, String spanId) {
        this.rawTask = task;
        this.traceId = traceId;
        this.spanId = spanId;
        this.callerThreadId = Thread.currentThread().getId();
    }

    protected ForkJoinTask<V> getRawTask() {
        return this.rawTask;
    }

    @Override
    public V getRawResult() {
        return this.rawTask.getRawResult();
    }

    @Override
    protected void setRawResult(V value) {
        ReflectUtils.getMethodWrapper(this.rawTask
                , "setRawResult", Object.class).invoke(value);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return ReflectUtils.getMethodWrapper(this.rawTask
                , "cancel", boolean.class)
                .invoke(mayInterruptIfRunning);
    }

    @Override
    protected boolean exec() {
        if (Objects.isNull(this.rawTask)) {
            return true;
        }

        MethodWrapper mw = ReflectUtils.getMethodWrapper(this.rawTask, "exec");
        if (this.callerThreadId == Thread.currentThread().getId()) {
            return mw.invoke();
        }

        try {
            AutoTraceCtx.setSpanId(AutoTraceCtx.generate());

            if (Objects.nonNull(traceId)) {
                AutoTraceCtx.setTraceId(traceId);
            }

            if (Objects.nonNull(spanId)) {
                AutoTraceCtx.setParentSpanId(spanId);
            }

            return mw.invoke();
        } finally {
            AutoTraceCtx.removeAll();
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
