package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

public class ObservableTest {

    @Test
    public void testCreateObservable() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(50);
                subscriber.onNext(30);
                subscriber.onNext(20);
                subscriber.onNext(110);
                subscriber.onCompleted();
            }
        });
    }
}
