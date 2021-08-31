package com.xyzcorp.instructor;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

public class BackpressureTest {

    @Test
    public void testBackpressure() {
        Flowable<Long> flowable = Flowable.create(emitter -> {
            long i = 0;
            while (true) {
                emitter.onNext(i++);
            }
        }, BackpressureStrategy.BUFFER);

        flowable
            .observeOn(Schedulers.newThread())
            .subscribe(x -> {
                    Thread.sleep(15);
                    System.out.println(x);
                },
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }
}
