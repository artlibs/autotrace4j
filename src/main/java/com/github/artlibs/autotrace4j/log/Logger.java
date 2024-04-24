package com.github.artlibs.autotrace4j.log;

import com.github.artlibs.autotrace4j.log.appender.AppenderCombiner;
import com.github.artlibs.autotrace4j.log.event.DefaultLogEvent;
import com.github.artlibs.autotrace4j.log.event.Level;
import com.github.artlibs.autotrace4j.log.event.LogEvent;

/**
 * 功能：日志对象
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class Logger {

    private final String name;
    private final AppenderCombiner<LogEvent> appenderCombiner;

    public Logger(String name, AppenderCombiner<LogEvent> appenderCombiner) {
        this.name = name;
        this.appenderCombiner = appenderCombiner;
    }

    public String getName() {
        return name;
    }

    private DefaultLogEvent buildLogEvent(Level level, String template, Object[] args) {
        return new DefaultLogEvent(level, Thread.currentThread().getName(), System.currentTimeMillis(), this, template, args);
    }

    public void debug(String template, Object... args) {
        appenderCombiner.append(buildLogEvent(Level.DEBUG, template, args));
    }

    public void info(String template, Object... args) {
        appenderCombiner.append(buildLogEvent(Level.INFO, template, args));
    }

    public void warn(String template, Object... args) {
        appenderCombiner.append(buildLogEvent(Level.WARN, template, args));
    }

    public void error(String template, Object... args) {
        appenderCombiner.append(buildLogEvent(Level.ERROR, template, args));
    }

}
