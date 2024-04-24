package com.github.artlibs.autotrace4j.log;

import com.github.artlibs.autotrace4j.log.appender.AppenderCombiner;
import com.github.artlibs.autotrace4j.log.appender.DefaultConsoleAppender;
import com.github.artlibs.autotrace4j.log.appender.DefaultFileAppender;
import com.github.artlibs.autotrace4j.log.event.LogEvent;
import com.github.artlibs.autotrace4j.log.layout.DefaultLayout;
import com.github.artlibs.autotrace4j.support.SystemUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.artlibs.autotrace4j.log.LogConstants.SYSTEM_PROPERTY_LOG_DIR;

/**
 * 功能：日志工厂
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class LoggerFactory {

    final private static ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    final private static AppenderCombiner<LogEvent> APPENDER_COMBINER;

    static {
        APPENDER_COMBINER = new AppenderCombiner<>();
        APPENDER_COMBINER.addAppender(new DefaultConsoleAppender(new DefaultLayout()));
        Optional
            .ofNullable(SystemUtils.getSysPropertyPath(SYSTEM_PROPERTY_LOG_DIR))
            .ifPresent(path -> APPENDER_COMBINER.addAppender(new DefaultFileAppender(new DefaultLayout(), path)));
    }

    static public Logger logger(Class<?> clazz) {
        return logger(clazz.getCanonicalName());
    }

    static public Logger logger(String name) {
        Logger logger = LOGGER_MAP.get(name);
        if (logger == null) {
            logger = new Logger(name, APPENDER_COMBINER);
            Logger preLogger = LOGGER_MAP.putIfAbsent(name, logger);
            logger = preLogger != null ? preLogger : logger;
        }
        return logger;
    }

}
