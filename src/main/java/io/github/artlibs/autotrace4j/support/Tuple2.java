package io.github.artlibs.autotrace4j.support;

/**
 * 功能：tuple 2
 *
 * @author suopovate
 * @author Fury
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class Tuple2<F, S> {
    private F first;
    private S second;

    public Tuple2(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @SuppressWarnings("unused")
    public void setFirst(F first) {
        this.first = first;
    }

    @SuppressWarnings("unused")
    public void setSecond(S second) {
        this.second = second;
    }

}
