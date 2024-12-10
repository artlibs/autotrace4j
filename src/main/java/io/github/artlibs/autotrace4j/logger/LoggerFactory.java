package io.github.artlibs.autotrace4j.logger;

import io.github.artlibs.autotrace4j.logger.appender.AppenderCombiner;
import io.github.artlibs.autotrace4j.logger.appender.FileAppender;
import io.github.artlibs.autotrace4j.logger.appender.ConsoleAppender;
import io.github.artlibs.autotrace4j.logger.event.Level;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;
import io.github.artlibs.autotrace4j.logger.layout.DefaultLayout;
import io.github.artlibs.autotrace4j.support.SystemUtils;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.artlibs.autotrace4j.logger.LogConstants.*;

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

        if (loggerEnabled()) {
            ConsoleAppender consoleAppender = new ConsoleAppender(new DefaultLayout(), System.out, System.err);
            consoleAppender.start();
            APPENDER_COMBINER.addAppender(consoleAppender);

            Path logFilePath = SystemUtils.getSysPropertyPath(SYSTEM_PROPERTY_LOG_DIR)
                    .orElse(SystemUtils.getSysTempDir());
            FileAppender fileAppender = new FileAppender(
                    new DefaultLayout(), logFilePath,
                    SystemUtils.getSysPropertyInteger(SYSTEM_PROPERTY_LOG_FILE_RETENTION)
                            .orElse(DEFAULT_LOG_FILE_RETENTION),
                    SystemUtils.getSysPropertyInteger(SYSTEM_PROPERTY_LOG_FILE_SIZE)
                            .orElse(DEFAULT_LOG_FILE_SIZE)
            );
            fileAppender.start();
            APPENDER_COMBINER.addAppender(fileAppender);
        }

        APPENDER_COMBINER.start();

        // level set
        LEVEL = getLevelConfig();
    }

    public static boolean loggerEnabled() {
        return Boolean.TRUE.equals(SystemUtils.getSysPropertyBool(SYSTEM_PROPERTY_LOG_ENABLE)
                .orElse(Boolean.FALSE));
    }

    private static Level getLevelConfig() {
        return Optional
            .ofNullable(System.getProperty(SYSTEM_PROPERTY_LOG_LEVEL))
            .map(String::toUpperCase)
            .map(Level::valueOf)
            .orElse(Level.DEBUG);
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
