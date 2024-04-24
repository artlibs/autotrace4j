package com.github.artlibs.autotrace4j.log.appender;

import com.github.artlibs.autotrace4j.log.event.DefaultLogEvent;
import com.github.artlibs.autotrace4j.log.event.Level;
import com.github.artlibs.autotrace4j.log.event.LogEvent;
import com.github.artlibs.autotrace4j.log.layout.Layout;

import java.util.Objects;

/**
 * 功能：默认日志输出-控制台
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultConsoleAppender extends AsyncAppender<LogEvent> {

    Layout<DefaultLogEvent> layout;

    public DefaultConsoleAppender(Layout<DefaultLogEvent> layout) {
        super();
        this.layout = layout;
        start();
    }

    @Override
    public boolean support(LogEvent event) {
        return event instanceof DefaultLogEvent;
    }

    @Override
    void doAppend(LogEvent event) {
        DefaultLogEvent defaultLogEvent = ((DefaultLogEvent) event);
        if (Objects.equals(event.getLevel(), Level.ERROR)){
            System.err.println(layout.format(defaultLogEvent));
        } else {
            System.out.println(layout.format(defaultLogEvent));
        }
    }

}
