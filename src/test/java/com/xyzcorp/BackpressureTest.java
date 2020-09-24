package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;

public class BackpressureTest {
    private Observable<Integer> crazyObservable;
    private Flowable<Integer> flowableObservable;

    @Before
    public void startUp() {
        crazyObservable = Observable.create(emitter -> {
            int i = 0;
            while (true) {
                emitter.onNext(i);
                i++;
            }
        });
    }

    @Test
    public void testBackpressure() {
        crazyObservable
            .observeOn(Schedulers.computation())
            .subscribe(integer -> {
                Thread.sleep(5);
                System.out.println(integer);
            }, Throwable::printStackTrace);
    }
}
