package io.github.artlibs.autotrace4j.logger.layout;

import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;
import io.github.artlibs.autotrace4j.support.ThrowableUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static io.github.artlibs.autotrace4j.support.Constants.*;

/**
 * 功能：默认日志格式化器
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class DefaultLayout implements Layout<LogEvent> {
    public static final String UNDEFINE = "undefine";

    @Override
    public String format(LogEvent event) {
        if (event != null && event.getMessage() != null) {
            Logger logger = event.getLogger();
            StringBuilder sb = new StringBuilder();
            appendItem(
                Optional
                    .ofNullable(event.getEventTime())
                    .map(eventTime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEventTime()), ZoneId.systemDefault()))
                    .map(LocalDateTime::toString)
                    .orElse(UNDEFINE),
                sb
            );
            appendItem(Optional.ofNullable(event.getThreadName()).orElse(UNDEFINE), sb);
            appendItem(Optional.ofNullable(event.getLevel()).map(Enum::name).orElse(UNDEFINE), sb);
            appendItem(Optional.ofNullable(logger).map(Logger::getName).orElse(UNDEFINE), sb);
            sb.append("-");
            sb.append(SPACE);
            sb.append(String.format(event.getMessage(), event.getArguments()));
            if (event.getThrowable() != null) {
                sb.append(System.lineSeparator());
                sb.append(ThrowableUtils.throwableToStr(event.getThrowable()));
            }
            sb.append(System.lineSeparator());
            return sb.toString();
        }
        return "";
    }

    private static void appendItem(String item, StringBuilder sb) {
        sb.append(LEFT_MIDDLE_BRACKET);
        sb.append(item);
        sb.append(RIGHT_MIDDLE_BRACKET);
        sb.append(SPACE);
    }

}
