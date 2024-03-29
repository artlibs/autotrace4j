package artlibs.test.unit.jdk;

import artlibs.test.unit.support.RunCase;
import com.github.artlibs.autotrace4j.support.TraceUtils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * ThreadPoolExecutor Test
 *
 * @author Fury
 * @since 2024-03-28
 * <p>
 * All rights Reserved.
 */
public class ThreadPoolExecutorCase implements RunCase {
    private final ExecutorService service;
    private ThreadPoolExecutorCase(ExecutorService service){
        this.service = service;
    }

    public static ThreadPoolExecutorCase newCase(ExecutorService service) {
        return new ThreadPoolExecutorCase(service);
    }

    @Override
    public boolean run() throws Exception {
        final String expectTraceId = "expect-trace-id";
        final Long unexpectThreadId = Thread.currentThread().getId();

        TraceUtils.setTraceId(expectTraceId);
        Wrap wrap = service.submit(() -> new Wrap(
                Thread.currentThread().getId(),
                TraceUtils.getTraceId())).get();

        return Objects.nonNull(wrap) &&
         expectTraceId.equals(wrap.getTraceId()) &&
         !unexpectThreadId.equals(wrap.getThreadId());
    }

    public static class Wrap {
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
