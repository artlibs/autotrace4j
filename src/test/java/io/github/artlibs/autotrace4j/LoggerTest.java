package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.logger.DefaultLogger;
import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.LoggerFactory;
import io.github.artlibs.autotrace4j.logger.appender.*;
import io.github.artlibs.autotrace4j.logger.event.Level;
import io.github.artlibs.autotrace4j.logger.event.LogEvent;
import io.github.artlibs.autotrace4j.logger.layout.DefaultLayout;
import io.github.artlibs.autotrace4j.support.SystemUtils;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

import static io.github.artlibs.autotrace4j.context.ReflectUtils.getDeclaredField;
import static io.github.artlibs.autotrace4j.context.ReflectUtils.getDeclaredFieldValue;
import static io.github.artlibs.autotrace4j.logger.event.Level.DEBUG;
import static io.github.artlibs.autotrace4j.logger.event.Level.INFO;
import static io.github.artlibs.autotrace4j.support.Constants.*;
import static io.github.artlibs.autotrace4j.support.FileUtils.deleteDirectoryRecursively;

/**
 * 功能：日志测试
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggerTest {
    protected static final Level TEST_DEFAULT_LEVEL = DEBUG;
    protected static final Path LOG_DIR = SystemUtils.getSysTempDir()
            .resolve("autotrace4j-test")
            .resolve(LoggerTest.class.getSimpleName());

    @BeforeAll
    public static void beforeAll() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
        System.setProperty(SYSTEM_PROPERTY_LOG_ENABLE, "true");
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        Files.createDirectories(LOG_DIR);
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
    }

    public void benchMark() throws InterruptedException, IllegalAccessException {
        Logger byName = LoggerFactory.getLogger(LoggerTest.class.getCanonicalName());
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byName.warn("benchMark");
        }
        waitingForLoggerFactoryAsyncAppend();
        long endTime = System.currentTimeMillis();
        System.out.println("toast: " + (endTime - startTime));
    }

    @Test
    @Order(1)
    void defaultLayout() {
        DefaultLayout defaultLayout = new DefaultLayout();
        Logger logger = newLogger(LoggerTest.class.getCanonicalName(), null, INFO);
        LogEvent logEvent = buildLogEvent(logger, "test", new Object[0]);
        String timeStr = buildItem(
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(logEvent.getEventTime()), ZoneId.systemDefault())
                .toString()
        );
        // [2024-04-27T17:37:22.166] [main] [INFO] [io.github.artlibs.autotrace4j.LoggerTest] - test
        Assertions.assertEquals(
            timeStr + " [main] [INFO] [io.github.artlibs.autotrace4j.LoggerTest] - test" + System.lineSeparator(),
            defaultLayout.format(logEvent)
        );

        logEvent = buildLogEvent(logger, "test%s%s%s%d", new Object[]{ "-", "format", "-", 1 });
        timeStr = buildItem(
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(logEvent.getEventTime()), ZoneId.systemDefault())
                .toString()
        );
        Assertions.assertEquals(
            timeStr + " [main] [INFO] [io.github.artlibs.autotrace4j.LoggerTest] - test-format-1" + System.lineSeparator(),
            defaultLayout.format(logEvent)
        );
    }

    @Test
    @Order(2)
    void logConsole() throws IOException, InterruptedException, IllegalAccessException {
        for (Level limitLevel : Level.values()) {
            ByteArrayOutputStream logCollectStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(logCollectStream);
            ConsoleAppender consoleAppender = new ConsoleAppender(
                new DefaultLayout(),
                printStream,
                printStream
            );
            consoleAppender.start();
            Logger logger = newLogger(LoggerTest.class.getCanonicalName(), consoleAppender, limitLevel);
            // test every limit level log print
            Assertions.assertNotNull(logger);

            // make all level's log
            for (Level level : Level.values()) {
                ReflectUtils
                    .getMethod(logger, level.name().toLowerCase(), String.class, Object[].class)
                    .invoke(level.name(), new Object[0]);
            }
            // waiting for log collect,its async write.
            waitingForAsyncAppend(consoleAppender);
            // check console log
            checkLogContents(
                logCollectStream.toString(StandardCharsets.UTF_8.name()).split(System.lineSeparator()),
                logger.getLevel()
            );

            consoleAppender.stop();
        }
    }

    @Test
    @Order(3)
    void logFile() throws IOException, InterruptedException, IllegalAccessException {
        Path logPath = LOG_DIR.resolve("logFile");
        Files.createDirectories(logPath);
        for (Level limitLevel : Level.values()) {
            // don't clean file
            FileAppender fileAppender = new FileAppender(new DefaultLayout(), logPath, 0);
            fileAppender.start();
            Logger logger = newLogger(LoggerTest.class.getCanonicalName(), fileAppender, limitLevel);
            // test every limit level log print
            // make all level's log
            for (Level level : Level.values()) {
                ReflectUtils
                    .getMethod(logger, level.name().toLowerCase(), String.class, Object[].class)
                    .invoke(level.name(), new Object[0]);
            }
            // waiting for log collect,its async write.
            waitingForAsyncAppend(fileAppender);
            // check file log
            Path logFile = logPath.resolve(FileAppender.dateToLogFileName(LocalDateTime.now()) + ".log");
            checkLogContents(
                new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8).split(System.lineSeparator()),
                logger.getLevel()
            );
            try (FileChannel fileChannel = FileChannel.open(logFile, StandardOpenOption.WRITE)) {
                // 将文件大小截断至0字节，相当于清空文件内容
                fileChannel.truncate(0);
            }
            fileAppender.stop();
        }
    }

    @Test
    @Order(4)
    void cleanExpiredFile() throws IOException, InterruptedException, ExecutionException {
        LocalDateTime now = LocalDateTime.now();
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path cleanExpiredFileDir = LOG_DIR.resolve("cleanExpiredFile");
        Files.createDirectories(cleanExpiredFileDir);
        int logFileRetentionDays = 7;
        FileAppender fileAppender = new FileAppender(
            new DefaultLayout(),
            cleanExpiredFileDir,
            logFileRetentionDays
        );
        // make expired log file, here we needn't create file,because the appender will create it.
        Path unExpiredFile1 = cleanExpiredFileDir.resolve(FileAppender.dateToLogFileName(now) + ".log");

        Path unExpiredFile2 = cleanExpiredFileDir.resolve(FileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays - 1)) + ".log");
        Files.createFile(unExpiredFile2);
        Path expiredFile1 = cleanExpiredFileDir
            .resolve(FileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays)) + ".log");
        Files.createFile(expiredFile1);
        Path expiredFile2 = cleanExpiredFileDir
            .resolve(FileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays + 1)) + ".log");
        Files.createFile(expiredFile2);

        System.out.println("before clean,log files: ");
        printDirectory(cleanExpiredFileDir);

        ScheduledFuture<?> future = ReflectUtils
            .getDeclaredMethod(fileAppender, "triggerCleanTask", LocalDateTime.class)
            .invoke(now);

        // waiting task finish
        Assertions.assertNotNull(future);
        future.get();

        System.out.println("after clean,log files: ");
        printDirectory(cleanExpiredFileDir);

        Assertions.assertTrue(Files.exists(unExpiredFile1));
        Assertions.assertTrue(Files.exists(unExpiredFile2));
        Assertions.assertFalse(Files.exists(expiredFile1));
        Assertions.assertFalse(Files.exists(expiredFile2));
    }

    @Test
    @Order(5)
    void rollingFile() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path rollingFileDir = LOG_DIR.resolve("rollingFile");
        Files.createDirectories(rollingFileDir);

        DefaultLayout defaultLayout = new DefaultLayout();
        FileAppender fileAppender = new FileAppender(defaultLayout, rollingFileDir, 0, 0);
        fileAppender.start();
        Logger logger = newLogger(LoggerTest.class.getCanonicalName(), fileAppender, INFO);

        String message = "test";
        String log = defaultLayout.format(buildLogEvent(logger, message, new Object[0]));
        int logSize = log.getBytes().length;

        System.out.println("before files: ");
        printDirectory(rollingFileDir);
        // 刚好消息跟文件大小相同
        ReflectUtils.setDeclaredFieldValue(fileAppender, "logFileSizeBytes", logSize);
        logger.info(message);
        waitingForAsyncAppend(fileAppender);
        try (Stream<Path> s = Files.list(rollingFileDir)) {
            Assertions.assertEquals(1, s.count());
        }
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 消息超过文件大小的情况,直接追加在当前文件后
        ReflectUtils.setDeclaredFieldValue(fileAppender, "logFileSizeBytes", logSize - 1);
        logger.info(message);
        waitingForAsyncAppend(fileAppender);
        try (Stream<Path> s = Files.list(rollingFileDir)) {
            Assertions.assertEquals(1, s.count());
        }
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 文件剩余空间不足以填充当前消息 且 消息大小小于文件大小时 roll 到下一个文件
        ReflectUtils.setDeclaredFieldValue(fileAppender, "logFileSizeBytes", logSize * 2 + 1);
        logger.info(message);
        logger.info(message);
        logger.info(message);
        waitingForAsyncAppend(fileAppender);
        try (Stream<Path> s = Files.list(rollingFileDir)) {
            Assertions.assertEquals(3, s.count());
        }
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        fileAppender.stop();
    }

    @Test
    @Order(6)
    void getLogger() {
        // test systemProperty set
        System.setProperty(SYSTEM_PROPERTY_LOG_LEVEL, TEST_DEFAULT_LEVEL.name());

        Logger byName = LoggerFactory.getLogger(LoggerTest.class.getCanonicalName());
        Logger byClass = LoggerFactory.getLogger(LoggerTest.class);

        Assertions.assertSame(byName, byClass);

        Assertions.assertNotNull(byName);
        Assertions.assertEquals(DEBUG, byName.getLevel());
        Assertions.assertEquals(LoggerTest.class.getCanonicalName(), byName.getName());
        AppenderCombiner<LogEvent> appenderCombiner = getDeclaredFieldValue(byName, "appender");
        Assertions.assertSame(
                getDeclaredFieldValue(LoggerFactory.class, "APPENDER_COMBINER"),
            appenderCombiner
        );
        boolean fileAppendExists = false;
        boolean consoleAppendExists = false;
        List<Appender<?>> appenderList = getDeclaredFieldValue(appenderCombiner, "appenderList");
        Assertions.assertNotNull(appenderList);
        for (Appender<?> appender : appenderList) {
            if (appender instanceof FileAppender) {
                fileAppendExists = true;
            }
            if (appender instanceof ConsoleAppender) {
                consoleAppendExists = true;
            }
        }
        // LoggerFactory加载比这个类早，所以appenderList一定是空
        boolean logDirPresent = SystemUtils.getSysPropertyPath(SYSTEM_PROPERTY_LOG_DIR).isPresent();
        Assertions.assertFalse(consoleAppendExists);
        if (logDirPresent){
            Assertions.assertFalse(fileAppendExists);
        }
    }

    private static Logger newLogger(String name, Appender<?> appender, Level level) {
        try {
            Constructor<DefaultLogger> declaredConstructor = DefaultLogger.class
                    .getDeclaredConstructor(String.class, Appender.class, Level.class);
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance(name, appender, level);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void waitingForLoggerFactoryAsyncAppend() throws IllegalAccessException, InterruptedException {
        List<Appender<?>> appenders = getDeclaredFieldValue(
                getDeclaredFieldValue(LoggerFactory.class, "APPENDER_COMBINER"),
            "appenderList"
        );
        boolean allEmpty = false;
        while (!allEmpty) {
            allEmpty = true;
            Assertions.assertNotNull(appenders);
            for (Appender<?> appender : appenders) {
                if (appender instanceof AsyncAppender) {
                    BlockingQueue<?> queue = (BlockingQueue<?>) getDeclaredField(AsyncAppender.class, "queue").get(appender);
                    if (queue != null) {
                        allEmpty &= queue.isEmpty();
                    }
                }
            }
            if (allEmpty) {
                Thread.sleep(1);
            }
        }
    }

    private static void waitingForAsyncAppend(AsyncAppender<?> asyncAppender) throws IllegalAccessException, InterruptedException {
        BlockingQueue<?> queue = (BlockingQueue<?>) getDeclaredField(AsyncAppender.class, "queue").get(asyncAppender);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Thread.yield();
            }
        }
        Thread.sleep(5);
    }

    private static void checkLogContents(String[] logs, Level limitLevel) {
        int allowLevelNum = Level.ERROR.ordinal() - limitLevel.ordinal() + 1;
        List<Level> allowLevels = new ArrayList<>();
        for (Level level : Level.values()) {
            if (level.compareTo(limitLevel) >= 0) {
                allowLevels.add(level);
            }
        }
        Assertions.assertNotNull(logs);
        Assertions.assertEquals(allowLevelNum, logs.length);
        for (int i = 0; i < allowLevelNum; i++) {
            // [2024-04-24T16:13:15.027] [main] [INFO] [io.github.artlibs.autotrace4j.LoggerTest] - INFO
            System.out.printf("check log content pass: %s%n", logs[i]);
            String[] logItems = logs[i].split(SPACE);
            Level level = allowLevels.get(i);
            Assertions.assertEquals(logItems[1], buildItem(Thread.currentThread().getName()));
            Assertions.assertEquals(logItems[2], buildItem(level.name()));
            Assertions.assertEquals(logItems[3], buildItem(LoggerTest.class.getCanonicalName()));
            Assertions.assertEquals(logItems[5], level.name());
        }
    }

    private static String buildItem(String item) {
        return LEFT_MIDDLE_BRACKET + item + RIGHT_MIDDLE_BRACKET;
    }

    private static void printDirectory(Path rollingFileDir) throws IOException {
        try(Stream<Path> s = Files.list(rollingFileDir)) {
            s.sorted(Comparator.comparing(Path::getFileName))
                    .forEach(System.out::println);
        }
    }

    private static LogEvent buildLogEvent(Logger logger, String message, Object[] args) {
        return ReflectUtils
            .getDeclaredMethod(logger, "buildLogEvent", Level.class, String.class, Object[].class)
            .invoke(INFO, message, args);
    }

}
