package io.github.artlibs.testsupport;

/**
 * Tuple
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class TupleResult {
    private Object value1;
    private Object value2;
    private Object value3;
    private Object value4;
    private Object value5;

    public TupleResult(){}
    public TupleResult(Object value1, Object value2){
        this.value1 = value1;
        this.value2 = value2;
    }
    public TupleResult(Object value1, Object value2, Object value3){
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public TupleResult(Object value1, Object value2, Object value3, Object value4){
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
    }

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Object value1) {
        this.value1 = value1;
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    public Object getValue3() {
        return value3;
    }

    public void setValue3(Object value3) {
        this.value3 = value3;
    }

    public Object getValue4() {
        return value4;
    }

    public void setValue4(Object value4) {
        this.value4 = value4;
    }

    public Object getValue5() {
        return value5;
    }

    public void setValue5(Object value5) {
        this.value5 = value5;
    }
}
