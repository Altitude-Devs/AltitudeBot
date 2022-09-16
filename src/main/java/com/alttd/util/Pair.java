package com.alttd.util;

public class Pair<X, Y> {

    private final X x;
    private final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getValue0() {
        return x;
    }

    public Y getValue1() {
        return y;
    }
}

