package io.github.artlibs.autotrace4j.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * 功能：系统工具
 *
 * @author suopovate
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class SystemUtils {
    private SystemUtils() {}

    /**
     * @throws java.nio.file.InvalidPathException if a {@code Path} object cannot be constructed from the abstract
     *                                            path (see {@link java.nio.file.FileSystem#getPath FileSystem.getPath})
     */
    public static Optional<Path> getSysPropertyPath(String name) {
        return Optional.ofNullable(System.getProperty(name))
                .map(path -> new File(System.getProperty(name)).toPath());
    }

    public static Optional<Boolean> getSysPropertyBool(String name) {
        return Optional.ofNullable(System.getProperty(name)).map(Boolean::parseBoolean);
    }

    public static Optional<Integer> getSysPropertyInteger(String name) {
        return Optional.ofNullable(System.getProperty(name)).map(Integer::parseInt);
    }

    public static Path getSysTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    /**
     * get the injectDir for store the class to be injected
     *
     * @param injectDir the real directory
     * @return the absolute path of injectDir
     */
    private static Path getClassInjectTempDirPath(String injectDir) {
        return getSysTempDir().resolve(Constants.INJECT_DIR_ROOT).resolve(injectDir);
    }

    /**
     * get the injectDir for store the class to be injected
     *
     * @param injectDir the real directory
     * @return the absolute path of injectDir
     */
    public static File getClassInjectTempDir(String injectDir) throws IOException {
        Path dirPath = getClassInjectTempDirPath(injectDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        return dirPath.toFile();
    }

}
