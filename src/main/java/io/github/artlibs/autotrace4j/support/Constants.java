package io.github.artlibs.autotrace4j.support;

/**
 * Constants definition
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public final class Constants {
    private Constants(){}

    public static final String DOT = ".";
    public static final String JAR = "jar";
    public static final String COMMA = ",";
    public static final String EMPTY = "";
    public static final String SLASH = "/";
    public static final String DOT_CLASS = ".class";
    public static final String GET_HEADER = "getHeader";
    public static final String SET_HEADER = "setHeader";
    public static final String GET_ATTRIBUTE = "getAttribute";
    public static final String SET_ATTRIBUTE = "setAttribute";
    public static final String INJECT_DIR_ROOT = "autotrace4j/inject";
    public static final String INJECT_DIR_BOOTSTRAP = "bootstrap";
    public static final String INTERCEPT_METHOD_NAME = "intercept";

    /* --------------- logging support --------------- */
    public static final String SPACE = " ";
    public static final String LEFT_MIDDLE_BRACKET = "[";
    public static final String RIGHT_MIDDLE_BRACKET = "]";
    public static final String CAUSED_BY = "Caused by: ";
    public static final String SYSTEM_PROPERTY_LOG_ENABLE = "autotrace4j.log.enable";
    public static final String SYSTEM_PROPERTY_LOG_DIR = "autotrace4j.log.dir";
    public static final String SYSTEM_PROPERTY_LOG_LEVEL = "autotrace4j.log.level";
    public static final String SYSTEM_PROPERTY_LOG_FILE_RETENTION = "autotrace4j.log.file.retention";
    public static final String SYSTEM_PROPERTY_LOG_FILE_SIZE = "autotrace4j.log.file.size";
    public static final int DEFAULT_LOG_FILE_RETENTION = 7;
    public static final int DEFAULT_LOG_FILE_SIZE = 0;
}
