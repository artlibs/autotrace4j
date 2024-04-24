package com.github.artlibs.autotrace4j.log.appender;

import com.github.artlibs.autotrace4j.log.Lifecycle;

/**
 * 功能：Appender,负责日志输出.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public interface Appender<E> extends Lifecycle {
    boolean support(E event);
    void append(E event);
}
