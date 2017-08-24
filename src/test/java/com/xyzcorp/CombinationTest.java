package com.xyzcorp;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class CombinationTest {

    @Test
    public void testMerge() throws Exception {
        Observable<String> fast = Observable.interval(4, TimeUnit.MILLISECONDS).map(x -> "F:" + x);
        Observable<String> slower = Observable.interval(50, TimeUnit.MILLISECONDS).map(x -> "S:" + x);
        Observable<String> observable = fast.mergeWith(slower);
        observable.subscribe(System.out::println);
        Thread.sleep(5000);
    }

    @Test
    public void testConcat() throws Exception {
        Observable<String> fast = Observable.interval(4, TimeUnit.MILLISECONDS).map(x -> "F:" + x).take(100);
        Observable<String> slower = Observable.interval(50, TimeUnit.MILLISECONDS).map(x -> "S:" + x).take(100);
        Observable<String> observable2 = Observable.concat(fast, slower);
        observable2.subscribe(System.out::println);
        Thread.sleep(5000);
    }



    @Test
    public void testZip() throws Exception {
        Observable<String> fast = Observable.interval(4, TimeUnit.MILLISECONDS).map(x -> "F:" + x).take(20);
        Observable<String> slower = Observable.interval(50, TimeUnit.MILLISECONDS).map(x -> "S:" + x);
        Observable<String> observable3 = fast.zipWith(slower, (i, x) -> i + ", " +  x);
        observable3.subscribe(System.out::println);
        Thread.sleep(5000);
    }

    @Test
    public void testCombineLatest() throws Exception {
        Observable<String> fast = Observable.interval(4, TimeUnit.MILLISECONDS).map(x -> "F:" + x).take(100);
        Observable<String> slower = Observable.interval(50, TimeUnit.MILLISECONDS).map(x -> "S:" + x).take(100);
        Observable<String> combineLatest = Observable.combineLatest(fast, slower, (i, x) -> i + x);
        combineLatest.subscribe(System.out::println);
        Thread.sleep(5000);
    }



    @Test
    public void testAmb() throws Exception {
        Observable<String> first = Observable.just("A").delay(10, TimeUnit.SECONDS);
        Observable<String> second = Observable.just("B").delay(2, TimeUnit.SECONDS);
        Observable<String> third = Observable.just("C").delay(5, TimeUnit.SECONDS);

        Observable.amb(Arrays.asList(first, second, third))
                  .doFinally(() -> System.out.println("Finally!"))
                  .subscribe(System.out::println);

        Thread.sleep(5000);
    }
}
