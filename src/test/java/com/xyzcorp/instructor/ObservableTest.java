package com.xyzcorp.instructor;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import org.junit.Test;

public class ObservableTest {
    @Test
    public void testBasicObservable() {
        //CMD_OPT_N = Inline
        //CMD+OPT+V = Introduce Variable

        Observable<Long> longObservable = Observable.<Long>create(emitter -> {
            emitter.onNext(10L);
            emitter.onNext(40L);
            emitter.onNext(50L);
            emitter.onComplete();
        });

        longObservable.subscribe(x -> System.out.println("S1:" + x));
        longObservable.subscribe(x -> System.out.println("S2:" + x));
    }

    public <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s", label, a, Thread.currentThread());
    }
}
