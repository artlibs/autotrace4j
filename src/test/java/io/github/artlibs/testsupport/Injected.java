package io.github.artlibs.testsupport;

import io.github.artlibs.autotrace4j.context.TraceContext;

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
                    TraceContext.getTraceId(),
                    TraceContext.getSpanId(),
                    TraceContext.getParentSpanId()
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
