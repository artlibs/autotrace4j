package io.github.artlibs.autotrace4j.logger;

import io.github.artlibs.autotrace4j.logger.event.Level;

/**
 * 功能：日志
 *
 * @author Fury
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public interface Logger {
    void trace(String message, Object... args);

    void debug(String message, Object... args);

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    String getName();

    Level getLevel();

    void setLevel(Level level);
}
