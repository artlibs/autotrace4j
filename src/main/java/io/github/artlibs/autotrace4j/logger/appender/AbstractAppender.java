package io.github.artlibs.autotrace4j.logger.appender;

/**
 * 功能：抽象appender
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public abstract class AbstractAppender<E> implements Appender<E> {

    private volatile boolean start;

    @Override
    public boolean start() {
        start = true;
        return true;
    }

    @Override
    public boolean stop() {
        start = false;
        return true;
    }

    @Override
    public boolean started() {
        return start;
    }

}
