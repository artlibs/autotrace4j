package io.github.artlibs.autotrace4j.logger.event;

import io.github.artlibs.autotrace4j.logger.Logger;

/**
 * 功能：默认日志事件
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class DefaultLogEvent extends AbstractLogEvent {
    @SuppressWarnings("unused")
    public DefaultLogEvent(Level level, String threadName, Long eventTime, Logger logger, String message, Object[] arguments) {
        super(level, threadName, eventTime, logger, message, arguments);
    }

    public DefaultLogEvent(
        Level level,
        String threadName,
        Long eventTime,
        Logger logger,
        String message,
        Object[] arguments,
        Throwable throwable
    ) {
        super(level, threadName, eventTime, logger, message, arguments, throwable);
    }

}
