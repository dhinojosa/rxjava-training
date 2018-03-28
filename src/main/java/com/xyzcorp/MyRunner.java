package com.xyzcorp;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class MyRunner {
    public static void main(String[] args) {
        Observable
                .interval(1, TimeUnit.SECONDS)
                .doOnNext(n -> System.out.println(Thread.currentThread().getName()))
                .map(x -> x * 40)
                .doOnNext(n -> System.out.println(Thread.currentThread().getName()))
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Completed"));
    }
}
