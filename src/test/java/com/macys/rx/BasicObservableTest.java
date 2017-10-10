package com.macys.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        System.out.println("-----");
        observable.subscribe(x -> System.out.println("On Next:" + x),
                Throwable::printStackTrace,
                () -> System.out.println("Done"));

        Thread.sleep(10000);
    }

    @Test
    public void testDefer() throws InterruptedException {
        Observable<LocalDateTime> observable =
                Observable
                        .defer(() -> {
                            System.out.println("We are being invoked again!");
                            return Observable.just(LocalDateTime.now());
                        });
        observable.subscribe(System.out::println);
        Thread.sleep(2000);
        System.out.println("-----");
        observable.repeat(10).subscribe(x -> System.out.println("On Next:" + x),
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    @Test
    public void testInterval() throws Exception {
        Observable.interval(1, TimeUnit.SECONDS)
                  .doOnNext(n -> System.out.println(Thread.currentThread().getName()))
                  .subscribe(System.out::println);
        Thread.sleep(10000);
    }


    @Test
    public void testIntervalRange() throws Exception {
        Observable.intervalRange(10, 20, 1, 2, TimeUnit.SECONDS)
                  .doOnNext(n -> System.out.println(Thread.currentThread().getName()))
                  .subscribe(System.out::println);
        Thread.sleep(20000);
    }

    @Test
    public void testRange() throws Exception {
        Observable.range(10, 20).subscribe(System.out::println);
    }

    @Test
    public void testManualCreationWithRepeatAndNoComplete() throws Exception {
        Observable.<Integer>create(s ->
        {
            s.onNext(1);
            s.onNext(2);
            s.onNext(3);
        })
                .map(x -> x + 1)
                .repeat(3)
                .subscribe(System.out::println,
                        Throwable::printStackTrace, () -> System.out.println("Done"));
        Thread.sleep(30000);
    }

    //DO NOT DO THIS!!!!
    @Test
    public void testViolationOfContractDoNotDoThisISwearDoNotThis() throws Exception {
        Observable.<Integer>create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        e.onNext(1);
                        e.onNext(2);
                    }
                };

                t.start();
                Thread t2 = new Thread() {
                    @Override
                    public void run() {
                        e.onNext(3);
                        e.onNext(4);
                    }
                };
                t2.start();
            }
        });
    }
    //DO NOT DO THIS!!!!

    @Test
    public void testClosureWithFilter() throws Exception {
        final int adult = 18;
        Observable
                .just(18, 4, 10, 22,
                        33, 50, 66)
                .filter(x -> x >= adult).subscribe(System.out::println);
    }

    @Test
    public void testFlatMap() throws Exception {
        Observable<Integer> observable =
                Observable.just(1, 2, 3, 4)
                          .flatMap(i -> Observable.just(-i, i, i + 1));

        observable.subscribe(System.out::println);
    }

    @Test
    public void testJava8StreamWithFlatMap() throws Exception {
        Stream.of(1,2,3,4).flatMap(x -> Stream.of(-x, x, x+1))
              .collect(Collectors.toList());
    }

    class MySubscriber implements org.reactivestreams.Subscriber<LocalDateTime> {

        @Override
        public void onSubscribe(Subscription s) {
           System.out.println("Got Subscription" + s);
           s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(LocalDateTime localDateTime) {
            System.out.println("On Next inside of thread:" + Thread.currentThread().getName());
            System.out.println(localDateTime);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();

        }

        @Override
        public void onComplete() {
            System.out.println("Done");
        }
    }
    @Test
    public void testFromCallable() throws InterruptedException {
        Flowable<LocalDateTime> localDateTimeObservable = Flowable
                .fromCallable(LocalDateTime::now).repeat(100);

        localDateTimeObservable.parallel(3).runOn(Schedulers.io()).map(x -> {
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(500);
            return x;
        }).subscribe(new Subscriber[]{new MySubscriber(), new MySubscriber(), new MySubscriber()});

        Thread.sleep(12000);

    }

    @Test
    public void testZip() throws Exception {

        Observable<Integer> range = Observable.range(1, 10);
        Observable<Character> characterObservable = Observable
                .range(97, 26).map(i -> (char) i.intValue());
        Observable<String> stringObservable = range.zipWith(characterObservable,
                (integer, character) -> "(" + integer + "," + character + ")");
        stringObservable.subscribe(System.out::println);
    }


    @Test
    public void testZipWithEmptyObservable() throws Exception {
        Observable<Integer> range = Observable.range(1, 10);
        Observable<Character> characterObservable = Observable.empty();
        Observable<String> stringObservable = range.zipWith(characterObservable,
                (integer, character) -> "(" + integer + "," + character + ")");
        stringObservable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

}
