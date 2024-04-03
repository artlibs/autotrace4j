package com.github.artlibs.testcase;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;

/**
 * Abstract Injected
 *
 * @author Fury
 * @since 2024-04-30
 * <p>
 * All rights Reserved.
 */
public interface Injected {
    Holder holder = new Holder();
    class Holder {
        private TupleResult injected;

        void setInjected() {
            injected = new TupleResult(
                    AutoTraceCtx.getTraceId(),
                    AutoTraceCtx.getSpanId(),
                    AutoTraceCtx.getParentSpanId()
            );
        }
        TupleResult getInjected() {
            return injected;
        }
    }

    default void graspInjected() {
        holder.setInjected();
    }
    default TupleResult getInjected() {
        return holder.getInjected();
    }
}
