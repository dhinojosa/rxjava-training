package com.xyzcorp;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HotColdObservableTest {
    private <A> void log(String label, A x) {
        System.out.format("%s:\t%s\t[%s]\n", label, x,
            Thread.currentThread().getName());
    }

    @Test
    public void testColdObservable() throws InterruptedException {
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
        Disposable d1 = interval.subscribe(x -> log("S1", x));
        Thread.sleep(3000);
        Disposable d2 = interval.subscribe(x -> log("S2", x));
        Thread.sleep(3000);
        Disposable d3 = interval.subscribe(x -> log("S3", x));
        Thread.sleep(3000);
        Disposable d4 = interval.subscribe(x -> log("S4", x));

        Thread.sleep(1000);
        System.out.println("d1 disposing");
        d1.dispose();
        Thread.sleep(1000);
        System.out.println("d2 disposing");
        d2.dispose();
        Thread.sleep(1000);
        System.out.println("d3 disposing");
        d3.dispose();
        Thread.sleep(5000);
        System.out.println("d4 disposing");
        d4.dispose();
        Thread.sleep(30000);
    }

    @Test
    public void testHotObservable() throws InterruptedException {
        Observable<Long> interval =
            Observable
                .interval(1, TimeUnit.SECONDS)
                .doOnNext(x -> log("Still Moving", x));

        Observable<Long> source = interval.publish().refCount();

        Disposable d1 = source.subscribe(x -> log("S1", x));
        Disposable d2 = source.subscribe(x -> log("S2", x));


        Thread.sleep(3000);
        Disposable d3 = source.subscribe(x -> log("S3", x));
        Thread.sleep(3000);
        Disposable d4 = source.subscribe(x -> log("S4", x));
        Thread.sleep(2000);
        d1.dispose();
        d2.dispose();
        d3.dispose();
        Thread.sleep(1000);
        d4.dispose();
        Thread.sleep(25000);


    }
}
