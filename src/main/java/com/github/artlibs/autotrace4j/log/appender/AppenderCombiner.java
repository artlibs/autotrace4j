package com.github.artlibs.autotrace4j.log.appender;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：appender 组合器
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class AppenderCombiner<E> extends AbstractAppender<E> {

    private List<Appender<E>> appenderList = new ArrayList<>();

    public AppenderCombiner() {
        start();
    }

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
