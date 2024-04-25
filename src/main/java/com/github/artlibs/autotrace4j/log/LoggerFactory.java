package com.github.artlibs.autotrace4j.log;

import com.github.artlibs.autotrace4j.log.appender.AppenderCombiner;
import com.github.artlibs.autotrace4j.log.appender.DefaultConsoleAppender;
import com.github.artlibs.autotrace4j.log.appender.DefaultFileAppender;
import com.github.artlibs.autotrace4j.log.event.Level;
import com.github.artlibs.autotrace4j.log.event.LogEvent;
import com.github.artlibs.autotrace4j.log.layout.DefaultLayout;
import com.github.artlibs.autotrace4j.support.SystemUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.artlibs.autotrace4j.log.LogConstants.SYSTEM_PROPERTY_LOG_DIR;
import static com.github.artlibs.autotrace4j.log.LogConstants.SYSTEM_PROPERTY_LOG_LEVEL;

/**
 * 功能：日志工厂
 * <p>
 * 暂提供如下配置(通过SystemProperty配置):
 * 1. autotrace4j.log.dir autotrace4j产生的日志文件存放目录
 * 2. autotrace4j.log.level autotrace4j产生的日志的最低级别,大于对应级别的日志才会被打印.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class LoggerFactory {

    final private static ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    final private static AppenderCombiner<LogEvent> APPENDER_COMBINER;

    volatile private static Level LEVEL;

    static {
        // appender set
        APPENDER_COMBINER = new AppenderCombiner<>();
        DefaultConsoleAppender defaultConsoleAppender = new DefaultConsoleAppender(new DefaultLayout());
        defaultConsoleAppender.start();
        APPENDER_COMBINER.addAppender(defaultConsoleAppender);
        Optional
            .ofNullable(SystemUtils.getSysPropertyPath(SYSTEM_PROPERTY_LOG_DIR))
            .ifPresent(path -> {
                DefaultFileAppender defaultFileAppender = new DefaultFileAppender(new DefaultLayout(), path);
                defaultFileAppender.start();
                APPENDER_COMBINER.addAppender(defaultFileAppender);
            });
        APPENDER_COMBINER.start();
        // level set
        LEVEL = getLevelConfig();
    }

    private static Level getLevelConfig() {
        return Optional
            .ofNullable(System.getProperty(SYSTEM_PROPERTY_LOG_LEVEL))
            .map(String::toUpperCase)
            .map(Level::valueOf)
            .orElse(Level.INFO);
    }

    public static Logger logger(Class<?> clazz) {
        return logger(clazz.getCanonicalName());
    }

    static public Logger logger(String name) {
        Logger logger = LOGGER_MAP.get(name);
        if (logger == null) {
            logger = new Logger(name, APPENDER_COMBINER, LEVEL);
            Logger preLogger = LOGGER_MAP.putIfAbsent(name, logger);
            logger = preLogger != null ? preLogger : logger;
        }
        return logger;
    }

}
