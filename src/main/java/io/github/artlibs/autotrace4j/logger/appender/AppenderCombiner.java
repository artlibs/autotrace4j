package io.github.artlibs.autotrace4j.logger.appender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能：appender 组合器
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class AppenderCombiner<E> extends AbstractAppender<E> {
    private final List<Appender<E>> appenderList = Collections
            .synchronizedList(new ArrayList<>(8));

    /**
     * appender 组合器
     */
    public AppenderCombiner() {
        // NO Sonar
    }

    /**
     * 添加 appender
     *
     * @param appender Appender
     */
    public void addAppender(Appender<E> appender) {
        if (appender != null) {
            appenderList.add(appender);
        }
    }

    @Override
    public boolean support(E event) {
        return true;
    }

    @Override
    public void append(E event) {
        if (started()) {
            for (Appender<E> appender : appenderList) {
                if (appender.support(event)) {
                    appender.append(event);
                }
            }
        }
    }

}
