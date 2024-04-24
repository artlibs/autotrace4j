package com.github.artlibs.autotrace4j.log.event;

import com.github.artlibs.autotrace4j.log.Logger;

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

    public AbstractLogEvent(Level level, String threadName, Long eventTime, Logger logger) {
        this.level = level;
        this.threadName = threadName;
        this.eventTime = eventTime;
        this.logger = logger;
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

}
