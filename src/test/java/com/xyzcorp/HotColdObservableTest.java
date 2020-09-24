package com.xyzcorp;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HotColdObservableTest {
    @Test
    public void testColdObservable() throws InterruptedException {
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
        interval.subscribe(i -> System.out.println("S1:" + i));
        Thread.sleep(5000);
        interval.subscribe(i -> System.out.println("S2:" + i));
        Thread.sleep(20000);
    }

    @Test
    public void testHotObservable() throws InterruptedException {
        ConnectableObservable<Long> connectableObservable = Observable.interval(1,
            TimeUnit.SECONDS).publish();
        Observable<Long> interval = connectableObservable.autoConnect();
        Thread.sleep(1000);
        interval.subscribe(i -> System.out.println("S1:" + i));
        Thread.sleep(5000);
        interval.subscribe(i -> System.out.println("S2:" + i));
        Thread.sleep(20000);
    }
}
