package com.github.artlibs.autotrace4j.logger.event;

import com.github.artlibs.autotrace4j.logger.Logger;

/**
 * 功能：默认日志事件
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultLogEvent extends AbstractLogEvent {

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
