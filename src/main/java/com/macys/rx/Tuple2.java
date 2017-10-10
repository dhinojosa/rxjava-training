package com.macys.rx;

import java.util.Objects;

public class Tuple2<T, U> {

    private T _1;
    private U _2;

    public Tuple2(T _1, U _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public T _1() {
        return _1;
    }

    public U _2() {
        return _2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1, tuple2._1) &&
                Objects.equals(_2, tuple2._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tuple2{");
        sb.append("_1=").append(_1);
        sb.append(", _2=").append(_2);
        sb.append('}');
        return sb.toString();
    }
}
