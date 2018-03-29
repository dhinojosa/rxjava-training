package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.concurrent.TimeUnit;


public class HotColdObservableTest {

    @Test
    public void testColdObservable() throws InterruptedException {
        Observable<Long> interval =
                Observable.interval(1, TimeUnit.SECONDS);

        interval.subscribe(i -> System.out.println("A:" + i));

        Thread.sleep(2000);

        interval.subscribe(i -> System.out.println("B:" + i));

        Thread.sleep(30000);
    }

    @Test
    public void testHotObservable() throws InterruptedException {
        Observable<Long> interval =
                Observable
                        .interval(1, TimeUnit.SECONDS)
                        .publish()
                        .autoConnect();

        interval.subscribe(i -> System.out.println("A:" + i));

        Thread.sleep(2000);

        interval.subscribe(i -> System.out.println("B:" + i));

        Thread.sleep(30000);
    }

    @Test
    public void testHotObservableWithRefCount() throws InterruptedException {
        Observable<Long> interval = Observable
                .interval(1, TimeUnit.SECONDS);

        Observable<Long> refCountInterval =
                interval.publish()
                        .refCount();

        Subscription subscriptionA = refCountInterval
                                       .map(x -> "****" + x)
                                       .subscribe(i -> System.out.println("A:" + i),
                                                  e ->{},
                                               () -> System.out.println("Done"));
        Thread.sleep(2000);
        Subscription subscriptionB = refCountInterval
                                       .subscribe(i -> System.out.println("B:" + i));
        Thread.sleep(1000);
        subscriptionA.unsubscribe();

        Thread.sleep(5000);
        subscriptionB.unsubscribe();

        System.out.println("Was expecting a completed");

        Thread.sleep(30000);
    }
}
