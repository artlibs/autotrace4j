package io.github.artlibs.autotrace4j.logger;

import io.github.artlibs.autotrace4j.logger.appender.AppenderCombiner;
import io.github.artlibs.autotrace4j.logger.appender.DefaultFileAppender;
import io.github.artlibs.autotrace4j.logger.appender.DefaultPrintStreamAppender;
import io.github.artlibs.autotrace4j.logger.event.Level;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;
import io.github.artlibs.autotrace4j.logger.layout.DefaultLayout;
import io.github.artlibs.autotrace4j.support.SystemUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
public final class LoggerFactory {
    private LoggerFactory(){}

    private static final ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    private static final AppenderCombiner<LogEvent> APPENDER_COMBINER;

    private static final Level LEVEL;

    static {
        // appender set
        APPENDER_COMBINER = new AppenderCombiner<>();
        DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(new DefaultLayout(), System.out, System.err);
        defaultPrintStreamAppender.start();
        APPENDER_COMBINER.addAppender(defaultPrintStreamAppender);
        SystemUtils.getSysPropertyPath(LogConstants.SYSTEM_PROPERTY_LOG_DIR)
            .ifPresent(path -> {
                DefaultFileAppender defaultFileAppender = new DefaultFileAppender(
                    new DefaultLayout(),
                    path,
                    SystemUtils
                        .getSysPropertyInteger(LogConstants.SYSTEM_PROPERTY_LOG_FILE_RETENTION)
                        .orElse(LogConstants.DEFAULT_LOG_FILE_RETENTION),
                    SystemUtils
                        .getSysPropertyInteger(LogConstants.SYSTEM_PROPERTY_LOG_FILE_SIZE)
                        .orElse(LogConstants.DEFAULT_LOG_FILE_SIZE)
                );
                defaultFileAppender.start();
                APPENDER_COMBINER.addAppender(defaultFileAppender);
            });
        APPENDER_COMBINER.start();
        // level set
        LEVEL = getLevelConfig();
    }

    private static Level getLevelConfig() {
        return Optional
            .ofNullable(System.getProperty(LogConstants.SYSTEM_PROPERTY_LOG_LEVEL))
            .map(String::toUpperCase)
            .map(Level::valueOf)
            .orElse(Level.INFO);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getCanonicalName());
    }

    public static Logger getLogger(String name) {
        Logger logger = LOGGER_MAP.get(name);
        if (logger == null) {
            logger = new Logger(name, APPENDER_COMBINER, LEVEL);
            Logger preLogger = LOGGER_MAP.putIfAbsent(name, logger);
            logger = preLogger != null ? preLogger : logger;
        }
        return logger;
    }

}
