package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.context.ReflectUtils;
import io.github.artlibs.autotrace4j.logger.LogConstants;
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

import static io.github.artlibs.autotrace4j.context.ReflectUtils.getField;
import static io.github.artlibs.autotrace4j.context.ReflectUtils.getFieldValue;
import static io.github.artlibs.autotrace4j.logger.event.Level.INFO;
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

    protected static final Level TEST_DEFAULT_LEVEL = INFO;
    protected static final Path LOG_DIR = SystemUtils.getSysTempDir().resolve("test").resolve(LoggerTest.class.getSimpleName());

    @BeforeAll
    public static void beforeAll() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
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
        Logger byName = LoggerFactory.logger(LoggerTest.class.getCanonicalName());
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
    public void defaultLayout() {
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

    private static Logger newLogger(String name, Appender<?> appender, Level level) {
        try {
            Constructor<Logger> declaredConstructor = Logger.class.getDeclaredConstructor(String.class, Appender.class, Level.class);
            declaredConstructor.setAccessible(true);
            return declaredConstructor
                .newInstance(name, appender, level);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    public void logConsole() throws IOException, InterruptedException, IllegalAccessException {
        for (Level limitLevel : Level.values()) {
            ByteArrayOutputStream logCollectStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(logCollectStream);
            DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(
                new DefaultLayout(),
                printStream,
                printStream
            );
            defaultPrintStreamAppender.start();
            Logger logger = newLogger(LoggerTest.class.getCanonicalName(), defaultPrintStreamAppender, limitLevel);
            // test every limit level log print
            Assertions.assertNotNull(logger);

            // make all level's log
            for (Level level : Level.values()) {
                ReflectUtils
                    .getMethodWrapper(logger, level.name().toLowerCase(), String.class, Object[].class)
                    .invoke(level.name(), new Object[0]);
            }
            // waiting for log collect,its async write.
            waitingForAsyncAppend(defaultPrintStreamAppender);
            // check console log
            checkLogContents(
                logCollectStream.toString(StandardCharsets.UTF_8.name()).split(System.lineSeparator()),
                logger.getLevel()
            );

            defaultPrintStreamAppender.stop();
        }
    }

    @Test
    @Order(3)
    public void logFile() throws IOException, InterruptedException, IllegalAccessException {
        Path logPath = LOG_DIR.resolve("logFile");
        Files.createDirectories(logPath);
        for (Level limitLevel : Level.values()) {
            // don't clean file
            DefaultFileAppender defaultFileAppender = new DefaultFileAppender(new DefaultLayout(), logPath, 0);
            defaultFileAppender.start();
            Logger logger = newLogger(LoggerTest.class.getCanonicalName(), defaultFileAppender, limitLevel);
            // test every limit level log print
            // make all level's log
            for (Level level : Level.values()) {
                ReflectUtils
                    .getMethodWrapper(logger, level.name().toLowerCase(), String.class, Object[].class)
                    .invoke(level.name(), new Object[0]);
            }
            // waiting for log collect,its async write.
            waitingForAsyncAppend(defaultFileAppender);
            // check file log
            Path logFile = logPath.resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now()));
            checkLogContents(
                new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8).split(System.lineSeparator()),
                logger.getLevel()
            );
            try (FileChannel fileChannel = FileChannel.open(logFile, StandardOpenOption.WRITE)) {
                // 将文件大小截断至0字节，相当于清空文件内容
                fileChannel.truncate(0);
            }
            defaultFileAppender.stop();
        }
    }

    @Test
    @Order(4)
    public void cleanExpiredFile() throws IOException, InterruptedException, ExecutionException {
        LocalDateTime now = LocalDateTime.now();
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path cleanExpiredFileDir = LOG_DIR.resolve("cleanExpiredFile");
        Files.createDirectories(cleanExpiredFileDir);
        Integer logFileRetentionDays = 7;
        DefaultFileAppender defaultFileAppender = new DefaultFileAppender(
            new DefaultLayout(),
            cleanExpiredFileDir,
            logFileRetentionDays
        );
        // make expired log file, here we needn't create file,because the appender will create it.
        Path unExpiredFile1 = cleanExpiredFileDir.resolve(DefaultFileAppender.dateToLogFileName(now));
        Path unExpiredFile2 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays - 1)));
        Files.createFile(unExpiredFile2);
        Path expiredFile1 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays)));
        Files.createFile(expiredFile1);
        Path expiredFile2 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays + 1)));
        Files.createFile(expiredFile2);

        System.out.println("before clean,log files: ");
        printDirectory(cleanExpiredFileDir);

        ScheduledFuture<?> future = ReflectUtils
            .getMethodWrapper(defaultFileAppender, "triggerCleanTask", true, LocalDateTime.class)
            .invoke(now);

        // waiting task finish
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
    public void rollingFile() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path rollingFileDir = LOG_DIR.resolve("rollingFile");
        Files.createDirectories(rollingFileDir);

        DefaultLayout defaultLayout = new DefaultLayout();
        DefaultFileAppender defaultFileAppender = new DefaultFileAppender(defaultLayout, rollingFileDir, 0, 0);
        defaultFileAppender.start();
        Logger logger = newLogger(LoggerTest.class.getCanonicalName(), defaultFileAppender, INFO);

        String message = "test";
        String log = defaultLayout.format(buildLogEvent(logger, message, new Object[0]));
        int logSize = log.getBytes().length;

        System.out.println("before files: ");
        printDirectory(rollingFileDir);
        // 刚好消息跟文件大小相同
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize, true);
        logger.info(message);
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(Files.list(rollingFileDir).count(), 1);
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 消息超过文件大小的情况,直接追加在当前文件后
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize - 1, true);
        logger.info(message);
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(Files.list(rollingFileDir).count(), 1);
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 文件剩余空间不足以填充当前消息 且 消息大小小于文件大小时 roll 到下一个文件
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize * 2 + 1, true);
        logger.info(message);
        logger.info(message);
        logger.info(message);
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(3, Files.list(rollingFileDir).count());
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        defaultFileAppender.stop();
    }

    @Test
    @Order(6)
    public void getLogger() {
        // test systemProperty set
        System.setProperty(LogConstants.SYSTEM_PROPERTY_LOG_LEVEL, TEST_DEFAULT_LEVEL.name());
        System.setProperty(LogConstants.SYSTEM_PROPERTY_LOG_DIR, LOG_DIR.toString());

        Logger byName = LoggerFactory.logger(LoggerTest.class.getCanonicalName());
        Logger byClass = LoggerFactory.logger(LoggerTest.class);

        Assertions.assertSame(byName, byClass);

        Assertions.assertNotNull(byName);
        Assertions.assertEquals(INFO, byName.getLevel());
        Assertions.assertEquals(LoggerTest.class.getCanonicalName(), byName.getName());
        AppenderCombiner<LogEvent> appenderCombiner = getFieldValue(byName, "appender", true);
        Assertions.assertSame(
            getFieldValue(LoggerFactory.class, "APPENDER_COMBINER", true),
            appenderCombiner
        );
        boolean defaultPrintStreamAppendExists = false;
        boolean defaultFileAppendExists = false;
        List<Appender<?>> appenderList = getFieldValue(appenderCombiner, "appenderList", true);
        for (Appender<?> appender : appenderList) {
            if (appender instanceof DefaultPrintStreamAppender) {
                defaultPrintStreamAppendExists = true;
            }
            if (appender instanceof DefaultFileAppender) {
                defaultFileAppendExists = true;
            }
        }
        Assertions.assertTrue(defaultPrintStreamAppendExists);
        Assertions.assertTrue(defaultFileAppendExists);
    }

    private static void waitingForLoggerFactoryAsyncAppend() throws IllegalAccessException, InterruptedException {
        List<Appender<?>> appenders = getFieldValue(
            getFieldValue(LoggerFactory.class, "APPENDER_COMBINER", true),
            "appenderList",
            true
        );
        boolean allEmpty = false;
        while (!allEmpty) {
            allEmpty = true;
            for (Appender<?> appender : appenders) {
                if (appender instanceof AsyncAppender) {
                    BlockingQueue<?> queue = (BlockingQueue<?>) getField(AsyncAppender.class, "queue", true).get(appender);
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
        BlockingQueue<?> queue = (BlockingQueue<?>) getField(AsyncAppender.class, "queue", true).get(asyncAppender);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Thread.sleep(1);
            }
        }
        Thread.sleep(1);
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
            String[] logItems = logs[i].split(LogConstants.SPACE);
            Level level = allowLevels.get(i);
            Assertions.assertEquals(logItems[1], buildItem(Thread.currentThread().getName()));
            Assertions.assertEquals(logItems[2], buildItem(level.name()));
            Assertions.assertEquals(logItems[3], buildItem(LoggerTest.class.getCanonicalName()));
            Assertions.assertEquals(logItems[5], level.name());
        }
    }

    private static String buildItem(String item) {
        return LogConstants.LEFT_MIDDLE_BRACKET + item + LogConstants.RIGHT_MIDDLE_BRACKET;
    }

    private static void printDirectory(Path rollingFileDir) throws IOException {
        Files
            .list(rollingFileDir)
            .sorted(Comparator.comparing(Path::getFileName))
            .forEach(path -> System.out.println(path));
    }

    private static LogEvent buildLogEvent(Logger logger, String message, Object[] args) {
        return ReflectUtils
            .getMethodWrapper(logger, "buildLogEvent", true, Level.class, String.class, Object[].class)
            .invoke(INFO, message, args);
    }

}
