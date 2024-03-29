package testing.artlibs.autotrace4j.jdk;

import com.github.artlibs.autotrace4j.ctx.AutoTraceCtx;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * ScheduledThreadPoolExecutor, ThreadPoolExecutor Test
 *
 * @author Fury
 * @since 2024-03-28
 * <p>
 * All rights Reserved.
 */
public class ThreadPoolExecutorCase {
    private final ExecutorService service;
    private ThreadPoolExecutorCase(ExecutorService service){
        this.service = service;
    }

    public static ThreadPoolExecutorCase newCase(ExecutorService service) {
        return new ThreadPoolExecutorCase(service);
    }

    public boolean run(String expectedTraceId) throws Exception {
        final Long unexpectThreadId = Thread.currentThread().getId();

        AutoTraceCtx.setTraceId(expectedTraceId);
        Wrap wrap = service.submit(() -> new Wrap(
                Thread.currentThread().getId(),
                AutoTraceCtx.getTraceId())
        ).get();

        return Objects.nonNull(wrap) &&
                expectedTraceId.equals(wrap.getTraceId()) &&
                !unexpectThreadId.equals(wrap.getThreadId());
    }

    static class Wrap {
        private Long threadId;
        private String traceId;

        public Wrap(Long threadId, String traceId) {
            this.traceId = traceId;
            this.threadId = threadId;
        }

        public Long getThreadId() {
            return threadId;
        }

        public void setThreadId(Long threadId) {
            this.threadId = threadId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
    }
}
