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

    /**
     * 启动方法，将start设置为true并返回true
     *
     * @return 返回boolean类型，表示启动是否成功
     */
    @Override
    public boolean start() {
        start = true;
        return true;
    }

    /**
     * 停止方法，将start设置为false并返回true。
     *
     * @return 返回boolean类型，表示停止是否成功
     */
    @Override
    public boolean stop() {
        start = false;
        return true;
    }

    /**
     * 判断是否已启动。
     *
     * @return 如果已启动则返回true，否则返回false。
     */
    @Override
    public boolean started() {
        return start;
    }

}
