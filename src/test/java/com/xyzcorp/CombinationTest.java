package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CombinationTest {

    @Test
    public void testMerge() throws InterruptedException {
        Observable<String> o1 = Observable
                .interval(1, TimeUnit.SECONDS)
                .map(i -> "O1:" + i);

        Observable<String> o2 = Observable
                .interval(2, TimeUnit.SECONDS)
                .map(i -> "O2:" + i);

        o1.mergeWith(o2).subscribe(System.out::println);

        //Observable.merge(o1, o2)

        Thread.sleep(5000);
    }

    @Test
    public void testConcat() throws InterruptedException {
        Observable<String> o1 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O1:" + i).take(100);

        Observable<String> o2 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O2:" + i).take(100);

        o2.concatWith(o1).subscribe(System.out::println);

        Thread.sleep(5000);
    }

    @Test
    public void testAmb() throws InterruptedException {
        Observable<String> o1 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O1:" + i).take(100).delay(2, TimeUnit.SECONDS);

        Observable<String> o2 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O2:" + i).take(100).delay(1, TimeUnit.SECONDS);

        Observable.amb(o1, o2).subscribe(System.out::println);

        Thread.sleep(5000);
    }

    @Test
    public void testZip() throws InterruptedException {
        Observable<String> o1 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O1:" + i).take(100);

        Observable<String> o2 = Observable
                .interval(1, TimeUnit.SECONDS)
                .map(i -> "O2:" + i).take(100);

        Observable
                .zip(o1, o2, Tuple2::new)
                .subscribe(System.out::println);

        //o1.zipWith(o2, (e1, e2) -> new Tuple2<>(e1, e2));
        Thread.sleep(5000);
    }

    @Test
    public void testCombineLatest() throws InterruptedException {
        Observable<String> o1 = Observable
                .interval(5, TimeUnit.MILLISECONDS)
                .map(i -> "O1:" + i).take(100);

        Observable<String> o2 = Observable
                .interval(1, TimeUnit.SECONDS)
                .map(i -> "O2:" + i).take(100);

        Observable
                .combineLatest(o1, o2, Tuple2::new)
                .subscribe(System.out::println);

        //o1.zipWith(o2, (e1, e2) -> new Tuple2<>(e1, e2));
        Thread.sleep(5000);
    }
}
