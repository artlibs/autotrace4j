package com.github.artlibs.autotrace4j.support;

import java.util.LinkedList;
import java.util.List;

import static com.github.artlibs.autotrace4j.logger.LogConstants.CAUSED_BY;

/**
 * 功能：异常处理工具类
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class ThrowableUtils {

    private ThrowableUtils() {
    }

    public static String throwableToStr(Throwable t) {
        List<String> strList = new LinkedList<>();
        extract(strList, t, null);
        return String.join(System.lineSeparator(), strList);
    }

    private static void extract(List<String> strList, Throwable t, StackTraceElement[] parentSTE) {

        StackTraceElement[] ste = t.getStackTrace();
        final int numberOfcommonFrames = findNumberOfCommonFrames(ste, parentSTE);

        strList.add(formatFirstLine(t, parentSTE));
        for (int i = 0; i < (ste.length - numberOfcommonFrames); i++) {
            strList.add("\tat " + ste[i].toString());
        }

        if (numberOfcommonFrames != 0) {
            strList.add("\t... " + numberOfcommonFrames + " common frames omitted");
        }

        Throwable cause = t.getCause();
        if (cause != null) {
            ThrowableUtils.extract(strList, cause, ste);
        }
    }

    private static String formatFirstLine(Throwable t, StackTraceElement[] parentSTE) {
        String prefix = "";
        if (parentSTE != null) {
            prefix = CAUSED_BY;
        }

        String result = prefix + t.getClass().getName();
        if (t.getMessage() != null) {
            result += ": " + t.getMessage();
        }
        return result;
    }

    private static int findNumberOfCommonFrames(StackTraceElement[] ste, StackTraceElement[] parentSTE) {
        if (parentSTE == null) {
            return 0;
        }

        int steIndex = ste.length - 1;
        int parentIndex = parentSTE.length - 1;
        int count = 0;
        while (steIndex >= 0 && parentIndex >= 0) {
            if (ste[steIndex].equals(parentSTE[parentIndex])) {
                count++;
            } else {
                break;
            }
            steIndex--;
            parentIndex--;
        }
        return count;
    }

}
