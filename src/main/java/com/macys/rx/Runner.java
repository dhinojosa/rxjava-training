package com.macys.rx;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class Runner {

    public static void main(String[] args) {
        Observable.interval(1, TimeUnit.SECONDS)
                  .doOnNext(n -> System.out.println(Thread.currentThread().getName()))
                  .subscribe(System.out::println);
    }
}
