package io.github.artlibs.autotrace4j.logger;

import io.github.artlibs.autotrace4j.logger.appender.Appender;
import io.github.artlibs.autotrace4j.logger.event.DefaultLogEvent;
import io.github.artlibs.autotrace4j.logger.event.Level;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;

/**
 * 功能：日志对象
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class DefaultLogger implements Logger {
    private Level level;
    private final String name;
    private final Appender<LogEvent> appender;

    DefaultLogger(String name, Appender<LogEvent> appender, Level level) {
        this.name = name;
        this.appender = appender;
        this.level = level;
    }

    private LogEvent buildLogEvent(Level level, String message, Object[] args) {
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

    @Override
    public void trace(String message, Object... args) {
        append(Level.TRACE, message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        append(Level.DEBUG, message, args);
    }

    @Override
    public void info(String message, Object... args) {
        append(Level.INFO, message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        append(Level.WARN, message, args);
    }

    @Override
    public void error(String message, Object... args) {
        append(Level.ERROR, message, args);
    }

    private void append(Level level, String message, Object... args) {
        LogEvent event = buildLogEvent(level, message, args);
        if (event.getLevel().compareTo(this.getLevel()) >= 0) {
            appender.append(event);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    @SuppressWarnings("unused")
    public void setLevel(Level level) {
        this.level = level;
    }

}
