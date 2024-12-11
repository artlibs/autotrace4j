package io.github.artlibs.testsupport;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
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
public class JavaLoggingCase implements Injected {
    public static final List<Formatter> formatters = Arrays.asList(
            new JsonFormatter(), new SimpleFormatter()
    );

    public static InMemoryLogger getInMemoryLogger(Class<?> clazz, Formatter formatter) {
        Logger logger = Logger.getLogger(clazz.getName());

        InMemoryLogger handler = new InMemoryLogger(logger);
        handler.setFormatter(formatter);
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
                // 格式化日志并添加到列表
                String msg = getFormatter().format(record);

                System.out.println("msg: " + msg);

                logMessages.add(msg);
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

    public static class JsonFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return JSON.toJSONString(record);
        }
    }
}
