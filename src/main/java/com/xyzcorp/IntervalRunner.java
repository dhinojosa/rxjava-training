package com.xyzcorp;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.TimeUnit;

public class IntervalRunner {
    public static void main(String[] args) throws InterruptedException {
        Observable<Long> intervalObservable =
            Observable.interval(1, TimeUnit.SECONDS);
        intervalObservable.subscribe(
            System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"));
        Thread.currentThread().join();
    }
}
