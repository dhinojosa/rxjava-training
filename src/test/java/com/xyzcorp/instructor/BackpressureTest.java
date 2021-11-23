package com.xyzcorp.instructor;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

public class BackpressureTest {

    @Test
    public void testBackPressure() {
        Flowable<Long> longObservable =
            Flowable.create(emitter -> {
                long i = 0;
                while (true) {
                    emitter.onNext(i++);
                }
            }, BackpressureStrategy.MISSING);
        longObservable
            .onBackpressureBuffer(3000)
            .observeOn(Schedulers.newThread())
            .subscribe(item -> {
                Thread.sleep(5);
                System.out.println(item);
            }, Throwable::printStackTrace);
    }

    public <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s\n", label, a, Thread.currentThread());
    }

    @Test
    public void testObservableForkJoin() {
        Flowable.range(0, 1000)
                .parallel()
                .runOn(Schedulers.computation())
            .map(x -> x * 4)
            .filter(x -> x % 2 == 0)
            .doOnNext(x -> debug("branch", x))
            .sequential()
            .subscribe(s -> debug("final", s));
    }
}
