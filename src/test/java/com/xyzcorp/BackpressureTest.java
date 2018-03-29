package com.xyzcorp;

import org.junit.Before;
import org.junit.Test;
import rx.BackpressureOverflow;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class BackpressureTest {
    private Observable<Integer> crazyObservable;

    @Before
    public void startUp() {
        crazyObservable = Observable.create(subscriber -> {
            try {
                int i = 0;
                while (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(i++);
                }
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    @Test
    public void testWithoutBackPressureHandling() {
        crazyObservable
                .onBackpressureLatest()
                .onBackpressureDrop()
//                .onBackpressureBuffer(50000,
//                        () ->{},
//                        BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST
//                )
                .observeOn(Schedulers.computation())
                .subscribe(i -> {
                            try {
                                Thread.sleep(5);
                                System.out.println(i);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }, Throwable::printStackTrace,
                        () -> System.out.println("Done"));
    }
}
