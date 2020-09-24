package com.xyzcorp;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchedulerTest {
    @Test
    public void testScheduler() throws InterruptedException {
        ExecutorService executorService = Executors.newWorkStealingPool();
        Observable<Integer> integerObservable = Observable
            .range(1, 1000)
            .subscribeOn(Schedulers.from(executorService))
            .doOnEach(new ThreadObserver<>("CP0"))
            .observeOn(Schedulers.computation())
            .doOnEach(new ThreadObserver<>("CP1"));

        integerObservable
            .map(x -> x * 4).subscribe(System.out::println);

        integerObservable
            .observeOn(Schedulers.single())
            .doOnEach(new ThreadObserver<>("CP2"))
            .filter(x -> x % 2 == 0)
            .doOnEach(new ThreadObserver<>("CP3"))
            .observeOn(Schedulers.io())
            .doOnEach(new ThreadObserver<>("CP4"))
            .subscribe(System.out::println);

        Thread.sleep(10000);
    }
}
