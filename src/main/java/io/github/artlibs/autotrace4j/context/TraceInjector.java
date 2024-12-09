package io.github.artlibs.autotrace4j.context;

import java.util.Objects;

import static io.github.artlibs.autotrace4j.context.TraceContext.*;

/**
 * Trace Injector
 *
 * @author Fury
 * @since 2024-12-08
 * <p>
 * All rights Reserved.
 */
public interface TraceInjector {
    TraceInjector DF = new TraceInjector(){/* default trace injector */};

    /**
     * inject trace into log msg
     * @param logMsg -
     * @return -
     */
    default String injectTrace(String logMsg) {
        if (Objects.isNull(logMsg) || logMsg.trim().isEmpty()) {
            return logMsg;
        }

        try {
            String trimMsg = logMsg.trim();
            for (Format fmt : Format.values()) {
                if (fmt.detect(trimMsg)) {
                    return fmt.inject(logMsg, trimMsg);
                }
            }

            return Format.DEFAULT.inject(logMsg, logMsg.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logMsg;
    }

    enum Format {
        CSV {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean detect(String trimMsg) {
                // TODO: not support yet
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String inject(String logMsg, String trimMsg) {
                // TODO: not support yet
                return logMsg;
            }
        },
        JSON {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean detect(String trimMsg) {
                return trimMsg.startsWith("{") && trimMsg.endsWith("}");
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String inject(String logMsg, String trimMsg) {
                if (logMsg.contains(ATO_TRACE_ID) && logMsg.contains(getTraceId())) {
                    return logMsg;
                }

                String injectedTraceFields = "\"" + ATO_TRACE_ID +
                        QUOTE_COLON + getTraceId() + "\",";
                if (Objects.nonNull(getSpanId())) {
                    injectedTraceFields = injectedTraceFields + "\"" + ATO_SPAN_ID +
                            QUOTE_COLON + getSpanId() + "\",";
                }
                if (Objects.nonNull(getParentSpanId())) {
                    injectedTraceFields = injectedTraceFields + "\"" + ATO_PARENT_SPAN_ID +
                            QUOTE_COLON + getParentSpanId() + "\",";
                }
                trimMsg = "{" + injectedTraceFields + trimMsg.substring(1);

                return logMsg.endsWith("\n") ? trimMsg + "\n" : trimMsg;
            }
        },
        XML {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean detect(String trimMsg) {
                // TODO: not support yet
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String inject(String logMsg, String trimMsg) {
                // TODO: not support yet
                return logMsg;
            }
        },
        HTML {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean detect(String trimMsg) {
                // TODO: not support yet
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String inject(String logMsg, String trimMsg) {
                // TODO: not support yet
                return logMsg;
            }
        },
        DEFAULT {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean detect(String trimMsg) {
                // 默认当成纯文本字符串
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String inject(String logMsg, String trimMsg) {
                if (logMsg.trim().isEmpty() || logMsg.contains("[TraceId]" + getTraceId())) {
                    return logMsg;
                }

                String preTrimMessage = "[TraceId]" + getTraceId() + SEPARATOR;
                if (Objects.nonNull(getSpanId())) {
                    preTrimMessage += "[SpanId]" + getSpanId() + SEPARATOR;
                }
                if (Objects.nonNull(getParentSpanId())) {
                    preTrimMessage += "[P-SpanId]" + getParentSpanId() + SEPARATOR;
                }

                return preTrimMessage + logMsg;
            }
        },
        ;

        /**
         * 检测文本是否是指定格式
         * @param trimMsg -
         * @return -
         */
        abstract boolean detect(String trimMsg);

        /**
         * 注入trace到log消息中
         * @param logMsg -
         * @param trimMsg -
         * @return -
         */
        abstract String inject(String logMsg, String trimMsg);

        private static final String SEPARATOR = " - ";
        private static final String QUOTE_COLON = "\":\"";
    }
}
