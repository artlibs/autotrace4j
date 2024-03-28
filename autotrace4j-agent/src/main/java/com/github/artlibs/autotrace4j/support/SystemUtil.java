package com.github.artlibs.autotrace4j.support;

import com.sun.org.apache.bcel.internal.classfile.Field;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SystemUtil {

    public static String getSysTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * get the injectDir for store the class to be injected
     *
     * @param injectDir the real directory
     * @return the absolute path of injectDir
     */
    private static String getClassInjectTempDirPath(String injectDir) {
        return getSysTempDir() + CommonConstant.INJECT_DIR_ROOT + File.separator + injectDir;
    }

    /**
     * get the injectDir for store the class to be injected
     *
     * @param injectDir the real directory
     * @return the absolute path of injectDir
     */
    public static File getClassInjectTempDir(String injectDir) throws IOException {
        Path dirPath = Paths.get(getClassInjectTempDirPath(injectDir));
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        return dirPath.toFile();
    }

}
