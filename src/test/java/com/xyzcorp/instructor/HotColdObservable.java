package com.xyzcorp.instructor;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HotColdObservable {
    @Test
    public void testColdObservable() throws InterruptedException {
        Flowable<Long> f1 = Flowable.interval(1, TimeUnit.SECONDS);
        f1.subscribe(x -> System.out.println("S1:" + x));
        Thread.sleep(2000);
        f1.subscribe(x -> System.out.println("S2:" + x));
        Thread.sleep(30000);
    }

    private <A> void debug(String marker, A a) {
        System.out.printf("%s [%s]: %s\n", marker,
            Thread.currentThread(), a);
    }

    @Test
    public void testHotObservable() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Flowable<Long> f1 =
            Flowable.interval(1, TimeUnit.SECONDS)
                    .take(200)
                    .doOnNext(x -> System.out.println("Showing " + x))
                    .publish()
                    .refCount();

        Disposable disposable1 = f1.subscribe(x -> debug("S1", x),
            Throwable::printStackTrace, countDownLatch::countDown);
        Thread.sleep(5000);
        Disposable disposable2 =
            f1.observeOn(Schedulers.newThread()).subscribe(x -> debug("S2",
                x), Throwable::printStackTrace, countDownLatch::countDown);
        Thread.sleep(3000);
        disposable1.dispose();
        Thread.sleep(2000);
        disposable2.dispose();
        countDownLatch.await();
    }
}
