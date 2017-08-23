package com.xyzcorp;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class IntervalRunner {

    public static void main(String[] args) throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS).subscribe(System.out::println);
        Thread.sleep(5000);
    }
}
