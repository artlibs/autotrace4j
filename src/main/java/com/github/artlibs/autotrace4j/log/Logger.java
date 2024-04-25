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
    private Level level;

    public Logger(String name, AppenderCombiner<LogEvent> appenderCombiner, Level level) {
        this.name = name;
        this.appenderCombiner = appenderCombiner;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    private DefaultLogEvent buildLogEvent(Level level, String message, Object[] args) {
        Throwable throwable = null;
        Object[] resolvedArgs = args;
        if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
            throwable = ((Throwable) args[args.length - 1]);
            if (args.length > 1) {
                resolvedArgs = new Object[args.length - 1];
                System.arraycopy(args, 0, resolvedArgs, 0, args.length - 1);
            }
        }
        return new DefaultLogEvent(
            level,
            Thread.currentThread().getName(),
            System.currentTimeMillis(),
            this,
            message,
            resolvedArgs,
            throwable
        );
    }

    public void trace(String message, Object... args) {
        append(buildLogEvent(Level.TRACE, message, args));
    }

    public void debug(String message, Object... args) {
        append(buildLogEvent(Level.DEBUG, message, args));
    }

    public void info(String message, Object... args) {
        append(buildLogEvent(Level.INFO, message, args));
    }

    public void warn(String message, Object... args) {
        append(buildLogEvent(Level.WARN, message, args));
    }

    public void error(String message, Object... args) {
        append(buildLogEvent(Level.ERROR, message, args));
    }

    public void append(DefaultLogEvent event) {
        if (event.getLevel().compareTo(level) >= 0) {
            appenderCombiner.append(event);
        }
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
