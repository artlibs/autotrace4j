package com.github.log;

import com.github.artlibs.autotrace4j.context.ReflectUtils;
import com.github.artlibs.autotrace4j.log.LogConstants;
import com.github.artlibs.autotrace4j.log.Logger;
import com.github.artlibs.autotrace4j.log.LoggerFactory;
import com.github.artlibs.autotrace4j.log.appender.Appender;
import com.github.artlibs.autotrace4j.log.appender.AsyncAppender;
import com.github.artlibs.autotrace4j.log.appender.DefaultFileAppender;
import com.github.artlibs.autotrace4j.log.event.Level;
import com.github.artlibs.autotrace4j.support.SystemUtils;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static com.github.artlibs.autotrace4j.context.ReflectUtils.getField;
import static com.github.artlibs.autotrace4j.context.ReflectUtils.getFieldValue;
import static com.github.artlibs.autotrace4j.log.event.Level.WARN;
import static com.github.artlibs.autotrace4j.support.FileUtils.deleteDirectoryRecursively;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggerTest {

    protected static final Level TEST_DEFAULT_LEVEL = WARN;
    protected static final Path LOG_DIR = SystemUtils.getSysTempDir().resolve("test").resolve(LoggerTest.class.getSimpleName());

    @BeforeAll
    public static void beforeAll() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
        // test systemProperty set
        System.setProperty(LogConstants.SYSTEM_PROPERTY_LOG_LEVEL, TEST_DEFAULT_LEVEL.name());
        System.setProperty(LogConstants.SYSTEM_PROPERTY_LOG_DIR, LOG_DIR.toString());
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
        waitingForAsyncAppend();
        long endTime = System.currentTimeMillis();
        System.out.println("toast: " + (endTime - startTime));
    }

    @Test
    @Order(1)
    public void getLogger() {

        Logger byName = LoggerFactory.logger(LoggerTest.class.getCanonicalName());
        Assertions.assertNotNull(byName);
        Assertions.assertEquals(WARN, byName.getLevel());

        Logger byClass = LoggerFactory.logger(LoggerTest.class);
        Assertions.assertNotNull(byClass);
        Assertions.assertEquals(WARN, byClass.getLevel());


        Assertions.assertSame(byName, byClass);
    }

    @Test
    @Order(2)
    public void log() throws IOException, InterruptedException, IllegalAccessException {
        Logger byClass = LoggerFactory.logger(LoggerTest.class);
        // test every limit level log print
        for (Level limitLevel : Level.values()) {
            // note!!: we set logger level manually
            byClass.setLevel(limitLevel);
            Assertions.assertNotNull(byClass);
            PrintStream originalStream = System.out;
            ByteArrayOutputStream logCollectStream = new ByteArrayOutputStream();

            redirectOutStream(new PrintStream(logCollectStream));
            // make all level's log
            for (Level level : Level.values()) {
                ReflectUtils
                    .getMethodWrapper(byClass, level.name().toLowerCase(), String.class, Object[].class)
                    .invoke(level.name(), new Object[0]);
            }
            // waiting for log collect,its async write.
            waitingForAsyncAppend();
            redirectOutStream(originalStream);

            // check console log
            checkLogContents(logCollectStream.toString(StandardCharsets.UTF_8.name()).split("\n"), limitLevel);
            // check file log
            Path logFile = LOG_DIR.resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now()));
            checkLogContents(
                new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8).split("\n"),
                limitLevel
            );
            try (FileChannel fileChannel = FileChannel.open(logFile, StandardOpenOption.WRITE)) {
                // 将文件大小截断至0字节，相当于清空文件内容
                fileChannel.truncate(0);
            }
        }
    }

    @Test
    @Order(2)
    public void defaultLaylot() throws IOException, InterruptedException, IllegalAccessException {

    }

    @Test
    @Order(3)
    public void cleanExpiredFile() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path cleanExpiredFileTestDir = LOG_DIR.resolve("cleanExpiredFile");
        Files.createDirectories(cleanExpiredFileTestDir);
        DefaultFileAppender defaultFileAppender = new DefaultFileAppender(null, cleanExpiredFileTestDir);
        Integer logFileRetentionDays = ReflectUtils.getFieldValue(defaultFileAppender, "logFileRetentionDays");
        // make expired log file
        Path unExpiredFile1 = cleanExpiredFileTestDir.resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now()));
        Files.createFile(unExpiredFile1);
        Path unExpiredFile2 = cleanExpiredFileTestDir
            .resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now().minusDays(logFileRetentionDays - 1)));
        Files.createFile(unExpiredFile2);
        Path expiredFile1 = cleanExpiredFileTestDir
            .resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now().minusDays(logFileRetentionDays)));
        Files.createFile(expiredFile1);
        Path expiredFile2 = cleanExpiredFileTestDir
            .resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now().minusDays(logFileRetentionDays + 1)));
        Files.createFile(expiredFile2);

        Method cleanExpiredFiles = DefaultFileAppender.class.getDeclaredMethod("cleanExpiredFiles");
        cleanExpiredFiles.setAccessible(true);
        cleanExpiredFiles.invoke(defaultFileAppender);

        Assertions.assertTrue(Files.exists(unExpiredFile1));
        Assertions.assertTrue(Files.exists(unExpiredFile2));
        Assertions.assertFalse(Files.exists(expiredFile1));
        Assertions.assertFalse(Files.exists(expiredFile2));
    }

    private static void waitingForAsyncAppend() throws IllegalAccessException, InterruptedException {
        List<Appender<?>> appenders = getFieldValue(getFieldValue(LoggerFactory.class, "APPENDER_COMBINER"), "appenderList");
        boolean allEmpty = false;
        while (!allEmpty) {
            allEmpty = true;
            for (Appender<?> appender : appenders) {
                if (appender instanceof AsyncAppender) {
                    BlockingQueue<?> queue = (BlockingQueue<?>) getField(AsyncAppender.class, "queue").get(appender);
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
            // [2024-04-24T16:13:15.027] [main] [INFO] [com.github.log.LoggerTest] - INFO
            String[] logItems = logs[i].split(LogConstants.SPACE);
            Level level = allowLevels.get(i);
            Assertions.assertEquals(logItems[1], buildItem(Thread.currentThread().getName()));
            Assertions.assertEquals(logItems[2], buildItem(level.name()));
            Assertions.assertEquals(logItems[3], buildItem(LoggerTest.class.getCanonicalName()));
            Assertions.assertEquals(logItems[5], level.name());
            System.out.printf("check log content pass: %s%n", logs[i]);
        }
    }

    private static String buildItem(String item) {
        return LogConstants.LEFT_MIDDLE_BRACKET + item + LogConstants.RIGHT_MIDDLE_BRACKET;
    }

    private static void redirectOutStream(PrintStream stream) {
        System.setOut(stream);
        System.setErr(stream);
    }

}
