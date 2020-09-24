package com.xyzcorp;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;

public class BackpressureTest {
    private Observable<Integer> crazyObservable;
    private Flowable<Integer> crazyFlowable;

    @Before
    public void startUp() {
        crazyObservable = Observable.create(emitter -> {
            int i = 0;
            while (true) {
                emitter.onNext(i);
                i++;
            }
        });

        crazyFlowable = Flowable.create(emitter -> {
            int i = 0;
            while (true) {
                emitter.onNext(i);
                i++;
            }
        }, BackpressureStrategy.BUFFER);


    }

    @Test
    public void testObservableBackpressure() {
        crazyObservable
            .observeOn(Schedulers.computation())
            .subscribe(integer -> {
                Thread.sleep(5);
                System.out.println(integer);
            }, Throwable::printStackTrace);
    }

    @Test
    public void testFlowableBackpressure() {
        crazyFlowable
            .observeOn(Schedulers.computation())
            .subscribe(integer -> {
                Thread.sleep(5);
                System.out.println(integer);
            }, Throwable::printStackTrace);
    }
}
