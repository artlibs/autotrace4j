package io.github.artlibs.autotrace4j.support;

/**
 * 功能：tuple
 *
 * @author suopovate
 * @author Fury
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class Tuple2<First, Second> {
    private First first;
    private Second second;

    public Tuple2(First first, Second o2) {
        this.first = first;
        this.second = o2;
    }

    public First getFirst() {
        return first;
    }

    public Second getSecond() {
        return second;
    }

    public void setFirst(First first) {
        this.first = first;
    }

    public void setSecond(Second second) {
        this.second = second;
    }

}
