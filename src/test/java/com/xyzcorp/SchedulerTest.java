package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SchedulerTest {

    @Test
    public void testObserveOn() throws InterruptedException {
        //schedulers = thread pools
        //interval is on computational scheduler

        ExecutorService executorService = Executors.newCachedThreadPool();

        Observable
                .interval(1, TimeUnit.SECONDS)
                .doOnNext(x -> printCurrentThreadName("A")) //computation
                .map(x -> x * 2)
                .doOnNext(x -> printCurrentThreadName("B")) //computation
                .flatMap(x -> Observable.just(-x, x, x+3))
                .observeOn(Schedulers.newThread())
                .doOnNext(x -> printCurrentThreadName("C")) //new-thread
                .filter(x -> x % 3 != 0)
                .observeOn(Schedulers.from(executorService)) //cached-thread-pool
                .doOnNext(x -> printCurrentThreadName("D"))
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        ()-> System.out.println("Completed"));

        //Be very careful about update mutable objects
        Thread.sleep(40000);
    }

    private void printCurrentThreadName(String marker) {
        System.out.println(marker + ":" + Thread.currentThread().getName());
    }


    @Test
    public void testSubscribeOnOnly() throws InterruptedException {
        //schedulers = thread pools
        //interval is on computational scheduler

        ExecutorService executorService = Executors.newCachedThreadPool();

        Observable
                .range(1, 1000000)                          //new-thread
                .doOnNext(x -> printCurrentThreadName("A")) //new-thread
                .map(x -> x * 2)
                .doOnNext(x -> printCurrentThreadName("B")) //new-thread
                .flatMap(x -> Observable.just(-x, x, x+3))
                .subscribeOn(Schedulers.newThread())        //*
                .doOnNext(x -> printCurrentThreadName("C")) //new-thread
                .filter(x -> x % 3 != 0)
                .doOnNext(x -> printCurrentThreadName("D")) //new-thread
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        ()-> System.out.println("Completed"));

        //Be very careful about update mutable objects
        Thread.sleep(40000);
    }

    @Test
    public void testSubscribeOnWithObserveOn() throws InterruptedException {
        //schedulers = thread pools
        //interval is on computational scheduler

        ExecutorService executorService = Executors.newCachedThreadPool();

        Observable
                .range(1, 1000000)                          //new thread
                .doOnNext(x -> printCurrentThreadName("A")) //new-thread
                .map(x -> x * 2)
                .observeOn(Schedulers.newThread())
                //.doOnNext(x -> printCurrentThreadName("B")) //pool-thread
                .flatMap(x -> Observable.just(-x, x, x+3))
                .subscribeOn(Schedulers.computation())
                //.doOnNext(x -> printCurrentThreadName("C")) //pool-thread
                .filter(x -> x % 3 != 0)
                //.doOnNext(x -> printCurrentThreadName("D")) //pool-thread
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        ()-> System.out.println("Completed"));

        //Be very careful about update mutable objects
        Thread.sleep(40000);
    }
}
