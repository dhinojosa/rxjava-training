package com.xyzcorp.instructor;

import com.xyzcorp.MyPublisher;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservableTest {
    @Test
    public void testBasicObservable() {

        //CTRL+J = Documentation
        //CMD+P  = Show Parameters
        //CMD+D  = Duplicate Line
        //CMD+Delete = Remove Line

        //After f, s, t -> on next
        //After f onNext, After s onNext (this is my bet)
        Observable<Long> observable = Observable.create(emitter -> {
            System.out.println("ready to emit");
            emitter.onNext(10L);
            emitter.onNext(20L);
            emitter.onNext(30L);
            emitter.onComplete();
        });

        observable.subscribe(System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"));


        observable.subscribe(
            new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    System.out.println(aLong);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    System.out.println(e.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("Done");
                }
            }
        );
    }

    @Test
    public void testJust() {
        Observable<Long> longObservable =
            Observable.just(10L, 20L, 30L);
    }

    @Test
    public void testMyPublisher() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Observable<Long> longObservable =
            Observable.fromPublisher(new MyPublisher(executorService));

        Disposable disposable =
            longObservable.subscribe(System.out::println);

        Thread.sleep(2000);
        disposable.dispose();
    }


    @Test
    public void testRangeAndDebug() {
        Observable
            .range(1, 10)
            .doOnNext(integer -> ObservableTest.this.debug("A", integer))
            .map(i -> i * 2)
            .doOnEach(new GenericObserver<>("B"))
            .subscribe(System.out::println);
    }

    private <A> void debug(String marker, A a) {
        System.out.printf("%s [%s]: %s\n", marker,
            Thread.currentThread(), a);
    }
}
