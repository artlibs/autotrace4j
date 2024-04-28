package io.github.artlibs.autotrace4j.logger.appender;

import io.github.artlibs.autotrace4j.logger.event.Level;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;
import io.github.artlibs.autotrace4j.logger.exception.CreateAppenderException;
import io.github.artlibs.autotrace4j.logger.layout.Layout;

import java.io.PrintStream;
import java.util.Objects;

/**
 * 功能：默认日志输出-控制台
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultPrintStreamAppender extends AsyncAppender<LogEvent> {

    Layout<LogEvent> layout;
    private final PrintStream outPrintStream;
    private final PrintStream errPrintStream;

    public DefaultPrintStreamAppender(
        Layout<LogEvent> layout,
        PrintStream outPrintStream,
        PrintStream errPrintStream
    ) {
        super();
        if (Objects.isNull(outPrintStream)){
            throw new CreateAppenderException("outPrintStream missing.");
        }
        if (Objects.isNull(errPrintStream)){
            throw new CreateAppenderException("errPrintStream missing.");
        }
        this.layout = layout;
        this.outPrintStream = outPrintStream;
        this.errPrintStream = errPrintStream;
    }

    @Override
    public boolean support(LogEvent event) {
        return event != null;
    }

    @Override
    void doAppend(LogEvent event) {
        if (started()) {
            if (Objects.equals(event.getLevel(), Level.ERROR)) {
                errPrintStream.print(layout.format(event));
            } else {
                outPrintStream.print(layout.format(event));
            }
        }
    }

}
