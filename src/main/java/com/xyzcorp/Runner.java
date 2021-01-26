package com.xyzcorp;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.TimeUnit;

public class Runner {

    public static <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s\n", label, a, Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        Observable
            .interval(1, TimeUnit.SECONDS)
            .doOnNext(x -> debug("in don: ", x))
            .subscribe(System.out::println);
    }
}
