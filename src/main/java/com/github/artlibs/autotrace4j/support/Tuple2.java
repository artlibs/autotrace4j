package com.github.artlibs.autotrace4j.support;

/**
 * 功能：tuple
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class Tuple2<O1, O2> {

    O1 o1;
    O2 o2;

    public Tuple2(O1 o1, O2 o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public O1 getO1() {
        return o1;
    }

    public O2 getO2() {
        return o2;
    }

    public void setO1(O1 o1) {
        this.o1 = o1;
    }

    public void setO2(O2 o2) {
        this.o2 = o2;
    }

}
