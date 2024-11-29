package io.github.artlibs.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * java.util.logging.Logger
 * <p>
 * @author Fury
 * @since 2024-11-30
 * <p>
 * All rights Reserved.
 */
public class JavaLogging implements Injected {

    public static InMemoryLogger getInMemoryLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());

        InMemoryLogger handler = new InMemoryLogger(logger);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);

        return handler;
    }

    public static class InMemoryLogger extends Handler {
        private InMemoryLogger(Logger logger){
            this.logger = logger;
        }
        private final Logger logger;
        private final List<String> logMessages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (isLoggable(record)) {
                logMessages.add(getFormatter().format(record)); // 格式化日志并添加到列表
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {
            logMessages.clear(); // 清空日志
        }

        public Logger getLogger() {
            return logger;
        }

        public List<String> getMessages() {
            return logMessages;
        }
    }
}
