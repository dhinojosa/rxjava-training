package com.xyzcorp;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class CombinationTest {


    @Test
    public void testMergeConcatZip() throws Exception {
        Observable<Long> fast = Observable.interval(4, TimeUnit.MILLISECONDS);
        Observable<Long> slower = Observable.interval(50, TimeUnit.MILLISECONDS);

        System.out.println("merge ==========");

        Observable<Long> observable = fast.mergeWith(slower);
        observable.subscribe(System.out::println);

        System.out.println("combineLatest ==========");

        Observable<Long> combineLatest = Observable.combineLatest(fast, slower, (i, x) -> i + x);
        combineLatest.subscribe(System.out::println);



        //
//        System.out.println("concat ==========");
//
//        Observable<Long> observable2 = Observable.concat(fast, slower);
//        observable2.subscribe(System.out::println);
//
//        System.out.println("zip ==========");
//
//        Observable<Long> observable3 = fast.zipWith(slower, (i, x) -> i + x);
//        observable3.subscribe(System.out::println);

        Thread.sleep(5000);
    }


    @Test
    public void testAmb() throws Exception {
        Observable<String> first = Observable.just("A").delay(10, TimeUnit.SECONDS);
        Observable<String> second = Observable.just("B").delay(2, TimeUnit.SECONDS);
        Observable<String> third = Observable.just("C").delay(5, TimeUnit.SECONDS);

        Observable.amb(Arrays.asList(first, second, third)).subscribe(System.out::println);
        Thread.sleep(5000);
    }
}
