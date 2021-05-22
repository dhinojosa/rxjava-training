package com.xyzcorp;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

public class BackpressureTest {

    @Test
    public void testBackPressure() {
        Flowable<Long> flowable = Flowable.create(emitter -> {
            long i = 0;
            while (true) {
                emitter.onNext(i++);
            }
        }, BackpressureStrategy.DROP);

        flowable
            .observeOn(Schedulers.newThread())
            .onBackpressureDrop()
            .subscribe(i -> {
                Thread.sleep(50);
                System.out.println(i);
            }, Throwable::printStackTrace);
    }
}
