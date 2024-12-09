package io.github.artlibs.autotrace4j.logger.appender;

import io.github.artlibs.autotrace4j.support.ThrowableUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 功能：异步appender
 * <p>
 * 注意: doAppend是单独开启一个线程,串行处理所有的event.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public abstract class AsyncAppender<E> extends AbstractAppender<E> {

    /** 日志事件队列 */
    private final BlockingQueue<E> queue;
    /**
     * AsyncAppender构造函数，用于创建一个异步Appender实例。
     * 该构造函数会初始化一个LinkedBlockingQueue作为Appender内部的消息队列。
     */
    protected AsyncAppender() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean start() {
        Thread async = new Thread(() -> {
            while (true) {
                try {
                    if (started()) {
                        E event = queue.take();
                        doAppend(event);
                    }
                } catch (InterruptedException e) {
                    System.err.println(ThrowableUtils.throwableToStr(e));
                }
            }
        });

        // Fix: 设置成守护线程以防止主线程退出时阻止JVM的停止
        async.setDaemon(true);

        async.start();

        return super.start();
    }

    @Override
    public final void append(E event) {
        queue.offer(event);
    }

    abstract void doAppend(E event);

}
