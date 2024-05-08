package io.github.artlibs.autotrace4j.logger;

/**
 * 功能：日志常量
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class LogConstants {
    private LogConstants(){}

    public static final String SPACE = " ";
    public static final String LEFT_MIDDLE_BRACKET = "[";
    public static final String RIGHT_MIDDLE_BRACKET = "]";
    public static final String CAUSED_BY = "Caused by: ";

    public static final String SYSTEM_PROPERTY_LOG_DIR = "autotrace4j.log.dir";
    public static final String SYSTEM_PROPERTY_LOG_LEVEL = "autotrace4j.log.level";
    public static final String SYSTEM_PROPERTY_LOG_FILE_RETENTION = "autotrace4j.log.file.retention";
    public static final String SYSTEM_PROPERTY_LOG_FILE_SIZE = "autotrace4j.log.file.size";

    public static final int DEFAULT_LOG_FILE_RETENTION = 7;
    public static final int DEFAULT_LOG_FILE_SIZE = 0;

}
