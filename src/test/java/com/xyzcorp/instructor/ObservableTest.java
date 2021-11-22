package com.xyzcorp.instructor;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ObservableTest {
    @Test
    public void testBasicObservable() {
        //CMD_OPT_N = Inline
        //CMD+OPT+V = Introduce Variable

        Observable<Long> longObservable = Observable.<Long>create(emitter -> {
            Runnable runnable = () -> {
                emitter.onNext(10L);
                emitter.onNext(40L);
                emitter.onNext(50L);
                emitter.onComplete();
            };
            new Thread(runnable).start();
        });

        longObservable.subscribe(x -> debug("S1", x));
        longObservable.subscribe(x -> debug("S2", x));
    }

    public <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s\n", label, a, Thread.currentThread());
    }

    @Test
    public void testObservableJust() throws InterruptedException {
        Observable<String> afterScheduler =
            Observable.just(10, 50, 40)
                      .subscribeOn(Schedulers.computation())
                      .map(x -> x * 5)
                      .subscribeOn(Schedulers.io()) //ignored
                      .map(String::valueOf);
        afterScheduler.subscribe(x -> debug("S1", x));
        afterScheduler.subscribe(x -> debug("S2", x));
        Thread.sleep(1000);
    }

    @Test
    public void testDifferentWaysToCreateObservableOrFlowable() {

        //Futures are not lazy
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Observable<Integer> integerObservable =
            Observable.fromFuture(executorService.submit(() -> 40 * 5));
    }

    @Test
    public void testLazinessWithCallable() {
        Observable<Long> observable =
            Observable.fromCallable(() -> {
                System.out.println("We are ready to calculate");
                return 50 * 10L;
            });

        System.out.println("This should be the first message");

        observable.subscribe(x -> debug("S1", x));
    }

    @Test
    public void testSingleShouldWorkSameWay() {
        Single<Long> singleObservable =
            Single.fromCallable(() -> {
                System.out.println("We are ready to calculate");
                return 50 * 10L;
            });

        System.out.println("This should be the first message");

        singleObservable
            .subscribe(x -> debug("S1", x));
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                  .doOnNext(i -> debug("L1", i))
                  .map(i -> i * 3)
                  .doOnNext(i -> debug("L2", i))
                  .subscribe(i -> debug("On Next:", i));
        Thread.sleep(10000);
    }

    @Test
    public void testDefer() throws InterruptedException {
        Single<LocalDateTime> localDateTimeSingle =
           Single.defer(() -> Single.just(LocalDateTime.now())); //fresh

        localDateTimeSingle.subscribe(ldt -> debug("S1", ldt));
        Thread.sleep(1000);
        localDateTimeSingle.subscribe(ldt -> debug("S2", ldt));
        Thread.sleep(1000);
        localDateTimeSingle.subscribe(ldt -> debug("S3", ldt));
        Thread.sleep(1000);
    }
}




