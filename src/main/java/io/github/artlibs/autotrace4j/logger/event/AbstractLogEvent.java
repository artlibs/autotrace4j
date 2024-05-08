package io.github.artlibs.autotrace4j.logger.event;

import io.github.artlibs.autotrace4j.logger.Logger;

/**
 * 功能：日志事件抽象实现
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractLogEvent implements LogEvent {

    private final Level level;
    private final String threadName;
    private final Long eventTime;
    private final Logger logger;
    private final String message;
    private final Object[] arguments;
    private final Throwable throwable;


    protected AbstractLogEvent(
        Level level,
        String threadName,
        Long eventTime,
        Logger logger,
        String message,
        Object[] arguments
    ) {
        this(level, threadName, eventTime, logger, message, arguments, null);
    }

    protected AbstractLogEvent(
        Level level,
        String threadName,
        Long eventTime,
        Logger logger,
        String message,
        Object[] arguments,
        Throwable throwable
    ) {
        this.level = level;
        this.threadName = threadName;
        this.eventTime = eventTime;
        this.logger = logger;
        this.message = message;
        this.arguments = arguments;
        this.throwable = throwable;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public Long getEventTime() {
        return eventTime;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

}
