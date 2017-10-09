package com.macys.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class BasicObservableTest {
    @Test
    public void testBasicObservable() throws Exception {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                System.out.println("Observable Thread: " + Thread.currentThread().getName());
                e.onNext(40);
                e.onNext(30);
                e.onNext(20);
                e.onComplete();
            }
        });

        Observable<Integer> afterMap = observable.map(integer -> integer + 2);

        afterMap.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("On Subscribe: " + d);
            }

            @Override
            public void onNext(Integer t) {
                System.out.println("On Next Thread: " + Thread.currentThread().getName());
                System.out.println("On Next:" + t);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("On Error Thread: " + Thread.currentThread().getName());
                System.out.println("On Error:" + e);
            }

            @Override
            public void onComplete() {
                System.out.println("On Complete: " + Thread.currentThread().getName());
                System.out.println("Complete!");
            }
        });
    }

    @Test
    public void testBasicObservableInlineWithLambdas() throws Exception {
        Observable.<Integer>create(e -> {
            e.onNext(40);
            e.onNext(30);
            e.onNext(20);
            e.onComplete();
        }).map(integer -> integer + 2).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("On Subscribe: " + d);
            }

            @Override
            public void onNext(Integer t) {
                System.out.println("On Next:" + t);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("On Error:" + e);
            }

            @Override
            public void onComplete() {
                System.out.println("Complete!");
            }
        });
    }

    @Test
    public void testBasicObservableWithFork() throws Exception {
        Observable<Integer> afterMap = Observable.<Integer>create(e -> {
            e.onNext(40);
            e.onNext(30);
            e.onNext(20);
            e.onComplete();
        }).map(integer -> integer + 2);


        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("On Subscribe: " + d);
            }

            @Override
            public void onNext(Integer t) {
                System.out.println("On Next:" + t);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("On Error:" + e);
            }

            @Override
            public void onComplete() {
                System.out.println("Complete!");
            }
        };
        afterMap.subscribe(observer);
        afterMap.map(i -> "Hello: " + i).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("On Next: " + s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("On Error: " + e);
            }

            @Override
            public void onComplete() {
                System.out.println("On Complete");
            }
        });
    }

    @Test
    public void testBasicObservableWithForkAndActions() throws Exception {
        Observable<Integer> observable = Observable.<Integer>create(e -> {
            e.onNext(40);
            e.onNext(30);
            e.onNext(20);
            e.onComplete();
        });

        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
    }

    @Test
    public void testBasicObservableWithJustForkAndActions() throws Exception {
        Observable<Integer> observable = Observable.just
                (40, 30, 20);
        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
        Thread.sleep(2000);
        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
    }

    @Test
    public void testNonDeferWithJust() throws InterruptedException {
        Observable<LocalDateTime> observable =
                Observable.just(LocalDateTime.now());
        observable.subscribe(System.out::println);
        Thread.sleep(2000);
        observable.subscribe(System.out::println);
    }
    @Test
    public void testDefer() throws InterruptedException {
        Observable<LocalDateTime> observable =
                Observable
                        .defer(() -> Observable.just(LocalDateTime.now()));
        observable.subscribe(System.out::println);
        Thread.sleep(2000);
        observable.subscribe(System.out::println);
    }
}
