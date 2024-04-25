package com.github.artlibs.autotrace4j.log.appender;

import com.github.artlibs.autotrace4j.exception.CreateAppenderException;
import com.github.artlibs.autotrace4j.log.event.LogEvent;
import com.github.artlibs.autotrace4j.log.layout.Layout;
import com.github.artlibs.autotrace4j.support.ThrowableUtils;
import com.github.artlibs.autotrace4j.support.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 功能：默认日志输出-文件
 * <p>
 * 支持自动清理/日志文件按天滚动记录.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultFileAppender extends AsyncAppender<LogEvent> {

    private final Layout<LogEvent> layout;
    private final Path directory;
    private final AtomicReference<Tuple2<Path, FileChannel>> logFile;
    private final Lock fileOptionLock;
    private final Timer timer;
    /**
     * 默认最长保留七天
     * 注意: 如果可配置,本值最少要保留一天(当天日志不删除).
     */
    private final int logFileRetentionDays;
    private final static int WRITE_BUFFER_SIZE = 1024;
    ThreadLocal<ByteBuffer> logWriteBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(WRITE_BUFFER_SIZE));

    public DefaultFileAppender(Layout<LogEvent> layout, Path directory) {
        this(layout, directory, 7);
    }

    public DefaultFileAppender(Layout<LogEvent> layout, Path directory, int logFileRetentionDays) {
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
        this.logFile = new AtomicReference<>();
        this.fileOptionLock = new ReentrantLock();
        this.timer = new Timer("DefaultFileAppender-cleaner");
        this.logFileRetentionDays = Math.max(logFileRetentionDays, 1);
    }

    @Override
    public boolean start() {
        // 定时删除过期日志文件
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
            }, 3 * 1000, 60 * 60 * 1000);
        return super.start();
    }

    @Override
    public boolean stop() {
        timer.cancel();
        return super.stop();
    }

    @Override
    public boolean support(LogEvent event) {
        return event != null;
    }

    @Override
    void doAppend(LogEvent event) {
        LogEvent LogEvent = ((LogEvent) event);
        Tuple2<Path, FileChannel> logFile = getLogFile();
        if (Objects.nonNull(logFile)) {
            String message = layout.format(LogEvent);
            message += "\n";
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            int len = bytes.length;
            int rem = len;
            while (rem > 0) {
                int n = Math.min(rem, WRITE_BUFFER_SIZE);
                logWriteBuffer.get().clear();
                logWriteBuffer.get().put(bytes, (len - rem), n);
                logWriteBuffer.get().flip();
                try {
                    logFile.getO2().write(logWriteBuffer.get());
                } catch (IOException e) {
                    System.err.println(ThrowableUtils.throwableToStr(e));
                }
                rem -= n;
            }
        }
    }

    private Tuple2<Path, FileChannel> getLogFile() {
        String logFileName = dateToLogFileName(LocalDateTime.now());
        Tuple2<Path, FileChannel> fileAndChannel;
        while (!isValidLogFile(fileAndChannel = logFile.get())) {
            fileOptionLock.lock();
            try {
                if (isValidLogFile(fileAndChannel = logFile.get())) {
                    return fileAndChannel;
                }
                try {
                    Path path = directory.resolve(logFileName);
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

    private boolean isValidLogFile(Tuple2<Path, FileChannel> pathAndChannel) {
        if (Objects.isNull(pathAndChannel)) return false;
        Path path = pathAndChannel.getO1();
        String logFileName = path.toFile().getName();
        boolean sameFile = dateToLogFileName(LocalDateTime.now()).equals(logFileName);
        boolean exists = Files.exists(pathAndChannel.getO1());
        return sameFile && exists;
    }

    private void cleanExpiredFiles() throws IOException {
        System.out.println("[DefaultFileAppender] start clean expired log files.");
        // 定时清理过期的日志文件
        fileOptionLock.lock();
        try {
            LocalDateTime expiredTime = LocalDateTime
                .now()
                .with(LocalTime.MIN)
                .minusDays(logFileRetentionDays);
            Files.list(directory)
                .map(DefaultFileAppender::mapPathToLogFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Tuple2::getO1))
                .forEach(dateAndPath -> {
                    if (!dateAndPath.getO1().isAfter(expiredTime)) {
                        File file = dateAndPath.getO2().toFile();
                        file.delete();
                        System.out.printf("[DefaultFileAppender] expired log file [%s] deleted.%n", file.getName());
                    }
                });
        } finally {
            System.out.println("[DefaultFileAppender] clean expired log files finish.");
            fileOptionLock.unlock();
        }
    }

    private static Optional<Tuple2<LocalDateTime, Path>> mapPathToLogFile(Path path) {
        if (Files.isDirectory(path)) {
            return Optional.empty();
        }
        return logFileNameToDate(path.toFile().getName()).map(date -> new Tuple2<>(date, path));
    }

    public static Optional<LocalDateTime> logFileNameToDate(String logFileName) {
        try {
            return Optional.of(LocalDateTime.of(LocalDate.parse(logFileName, DateTimeFormatter.ISO_LOCAL_DATE), LocalTime.MIN));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static String dateToLogFileName(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
