package com.github.artlibs.testcase;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * TPE
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class TpeCase {
    private final ExecutorService service;
    private TpeCase(ExecutorService service){
        this.service = service;
    }

    public static TpeCase newCase(ExecutorService service) {
        return new TpeCase(service);
    }

    public boolean run(String traceId, String spanId) throws Exception {
        final String unexpectThreadId = String.valueOf(Thread.currentThread().getId());

        AutoTraceCtx.setTraceId(traceId);
        Tuple result = service.submit(() -> new Tuple(
                AutoTraceCtx.getTraceId(),
                AutoTraceCtx.getSpanId(),
                String.valueOf(Thread.currentThread().getId()))
        ).get();

        return Objects.nonNull(result) &&
                traceId.equals(result.getValue1()) &&
                Objects.nonNull(result.getValue2()) &&
                !unexpectThreadId.equals(result.getValue3());
    }
}
