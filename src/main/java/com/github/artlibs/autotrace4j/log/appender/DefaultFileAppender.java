package com.github.artlibs.autotrace4j.log.appender;

import com.github.artlibs.autotrace4j.exception.CreateAppenderException;
import com.github.artlibs.autotrace4j.log.event.DefaultLogEvent;
import com.github.artlibs.autotrace4j.log.event.LogEvent;
import com.github.artlibs.autotrace4j.log.layout.Layout;
import com.github.artlibs.autotrace4j.support.ThrowableUtils;
import com.github.artlibs.autotrace4j.support.Tuple2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 功能：默认日志输出-文件
 *
 * 支持自动清理/日志文件按天滚动记录.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultFileAppender extends AsyncAppender<LogEvent> {

    Layout<DefaultLogEvent> layout;
    Path directory;
    AtomicReference<Tuple2<Path, FileChannel>> logFile;
    Lock fileOptionLock;
    Timer timer;
    /**
     * 默认最长保留七天
     */
    int logFileRetentionDays = 7;
    private final static int BUFFER_SIZE = 1024;
    ThreadLocal<ByteBuffer> logWriteBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(1024));

    public DefaultFileAppender(Layout<DefaultLogEvent> layout, Path directory) {
        if (Objects.isNull(directory) || (Files.exists(directory) && !Files.isDirectory(directory))) {
            throw new CreateAppenderException("log directory missing, it will not record any log event");
        }
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new CreateAppenderException("log directory create failed.", e);
            }
        }
        this.layout = layout;
        this.directory = directory;
        logFile = new AtomicReference<>();
        fileOptionLock = new ReentrantLock();
        timer = new Timer("DefaultFileAppender-cleaner");
        start();
    }

    @Override
    public boolean start() {
        // 计算过期时间
        // todo 定时删除任务待开发
        timer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (started()) {
                            cleanExpiredFiles();
                        }
                    } catch (Exception e) {
                        System.err.println(ThrowableUtils.throwableToStr(e));
                    }
                }
            }, 1000, 5000);
        return super.start();
    }

    @Override
    public boolean support(LogEvent event) {
        return event instanceof DefaultLogEvent;
    }

    @Override
    void doAppend(LogEvent event) {
        DefaultLogEvent defaultLogEvent = ((DefaultLogEvent) event);
        Tuple2<Path, FileChannel> logFile = getLogFile();
        if (Objects.nonNull(logFile)) {
            String message = layout.format(defaultLogEvent);
            message += "\n";
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            int len = bytes.length;
            int rem = len;
            while (rem > 0) {
                int n = Math.min(rem, BUFFER_SIZE);
                logWriteBuffer.get().clear();
                logWriteBuffer.get().put(bytes, (len - rem), n);
                logWriteBuffer.get().flip();
                try {
                    logFile.getO2().write(logWriteBuffer.get());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                rem -= n;
            }
        }
    }

    private Tuple2<Path, FileChannel> getLogFile() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Tuple2<Path, FileChannel> fileAndChannel;
        while (!isValidLogFile(fileAndChannel = logFile.get())) {
            fileOptionLock.lock();
            try {
                if (isValidLogFile(fileAndChannel = logFile.get())) {
                    return fileAndChannel;
                }
                try {
                    Path path = directory.resolve(date);
                    FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                    fileAndChannel = new Tuple2<>(path, fileChannel);
                    logFile.set(fileAndChannel);
                } catch (IOException e) {
                    System.err.println("[DefaultFileAppender] log file create error.");
                    System.err.println(ThrowableUtils.throwableToStr(e));
                    return null;
                }
            } finally {
                fileOptionLock.unlock();
            }
        }
        return fileAndChannel;
    }

    private boolean isValidLogFile(Tuple2<Path, FileChannel> pathFileChannelTuple2) {
        if (Objects.isNull(pathFileChannelTuple2)) return false;
        String logFileName = pathFileChannelTuple2.getO1().toFile().getName();
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE).equals(logFileName);
    }

    private void cleanExpiredFiles() throws IOException {
        // 定时清理过期的日志文件
        fileOptionLock.lock();
        try {
            LocalDateTime expiredTime = LocalDateTime
                .now()
                .with(LocalTime.MIN)
                .minusDays(logFileRetentionDays);
            Files.list(directory)
                .map(DefaultFileAppender::mapPathToLogFile)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Tuple2::getO1))
                .forEach(dateAndPath -> {
                    if (dateAndPath.getO1().compareTo(expiredTime) <= 0) {
                        try {
                            Files.delete(dateAndPath.getO2());
                        } catch (IOException e) {
                            System.err.println("[DefaultFileAppender] expired log file delete failed.");
                        }
                    }
                });
        } finally {
            fileOptionLock.unlock();
        }
    }

    private static Tuple2<LocalDateTime, Path> mapPathToLogFile(Path path) {
        if (Files.isDirectory(path)) {
            return null;
        }
        try {
            LocalDateTime date = LocalDateTime.parse(
                path.toFile().getName(),
                DateTimeFormatter.ISO_LOCAL_DATE
            );
            return new Tuple2<>(date, path);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
