package com.github.artlibs.autotrace4j.context.jdk;

import com.github.artlibs.autotrace4j.context.ReflectUtils;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RunnableFuture;

/**
 * RunnableFuture Fork Join Task
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
public class WrapRfForkTask<V> extends WrapForkTask<V> implements RunnableFuture<V>  {
    private WrapRfForkTask(){
        super();
    }

    public WrapRfForkTask(ForkJoinTask<V> task, String traceId, String spanId) {
        super(task, traceId, spanId);
    }

    @Override
    public void run() {
        ReflectUtils.getMethodWrapper(this.getRawTask(), "run").invoke();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
