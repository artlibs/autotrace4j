package com.github.artlibs.autotrace4j.log.layout;

import com.github.artlibs.autotrace4j.log.Logger;
import com.github.artlibs.autotrace4j.log.event.DefaultLogEvent;
import com.github.artlibs.autotrace4j.support.ThrowableUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static com.github.artlibs.autotrace4j.log.LogConstants.*;

/**
 * 功能：默认日志格式化器
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultLayout implements Layout<DefaultLogEvent> {

    public static final String UNDEFINE = "undefine";

    @Override
    public String format(DefaultLogEvent event) {
        if (event != null && event.getTemplate() != null) {
            Object[] args = event.getArguments();
            Throwable throwable = null;
            if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
                throwable = ((Throwable) args[args.length - 1]);
                if (args.length > 1) {
                    args = new Object[args.length - 1];
                    System.arraycopy(event.getArguments(), 0, args, 0, args.length - 1);
                }
            }
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
            sb.append(String.format(event.getTemplate(), args));
            if (throwable != null) {
                sb.append("\n");
                sb.append(ThrowableUtils.throwableToStr(throwable));
            }
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
