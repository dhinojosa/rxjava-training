package com.macys.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        Stream.of(1, 2, 3, 4).flatMap(x -> Stream.of(-x, x, x + 1))
              .collect(Collectors.toList());
    }


    @Test
    public void testZip() throws Exception {
        Observable<Integer> rangeObservable = Observable.range(1, 10);
        Observable<Character> characterObservable = Observable
                .range(97, 26).map(i -> (char) i.intValue());

        Observable<String> stringObservable1 =
                Observable.zip(rangeObservable, characterObservable,
                        (integer, character) -> "(" + integer + "," + character + ")");


        Observable<String> stringObservable2 =
                rangeObservable.zipWith(characterObservable,
                        (integer, character) -> "(" + integer + "," + character + ")");

        stringObservable2.subscribe(System.out::println);
    }

    @Test
    public void testZipWithTuple2() throws Exception {
        Observable<Integer> rangeObservable = Observable.range(1, 10);
        Observable<Character> characterObservable = Observable
                .range(97, 26).map(i -> (char) i.intValue());
        Observable<Tuple2<Integer, Character>> zip = Observable.zip(rangeObservable, characterObservable, Tuple2::new);
        zip.subscribe(System.out::println);
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


    @Test
    public void testOptional() {
        Optional<String> kumar = Optional.of("Kumar");
        String result = kumar.orElse("Nothing");
        System.out.println(result);
    }

    @Test
    public void testReduce() {
        Maybe<Integer> integerMaybe = Observable.range(1, 10).reduce(
                (total, next) -> {
                    System.out.println("total: " + total + "; next " + next);
                    return total + next;
                });

        integerMaybe.subscribe(System.out::println, Throwable::printStackTrace,
                () -> System.out.println("Done"));

        integerMaybe.subscribe(new MaybeObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {
                System.out.println(integer);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });
    }

    @Test
    public void testFunctionalFactorial() {
        int end = 5;
        Single<Integer> observable =
                Observable.range(1, end)
                          .reduce(1, (total, next) -> total * next);
        observable.subscribe(System.out::println);
    }

    @Test
    public void testUsingCollectionAsReturnForReduce() {
        int end = 5;
        Single<ArrayList<Integer>> arrayListSingle =
                Observable.range(1, end)
                          .reduce(new ArrayList<>(), (integers, integer) -> {
                              integers.add(integer);
                              return integers;
                          });
        arrayListSingle.subscribe(System.out::println);
    }

    @Test
    public void testCollect() {
        Single<List<String>> single = Observable.range(1, 20)
                                                .collect(ArrayList::new,
                                                        (strings, integer) -> strings.add("Hello!" + integer));

        single.subscribe(System.out::println);
    }

    @Test
    public void testCollectWithDefaultElements() {
        Single<List<String>> single = Observable.range(1, 20)
                                                .collect(() -> {
                                                            ArrayList<String> list = new ArrayList<>();
                                                            list.add("Foo");
                                                            list.add("Bar");
                                                            return list;
                                                        },
                                                        (strings, integer) -> strings.add("Hello!" + integer));

        single.subscribe(System.out::println);
    }


    @Test
    public void testCollectWithDefaultElementsWithConcat() {
        Observable<String> stringObservable = Observable.concat(
                Observable.just("Foo", "Bar"),
                Observable.range(1, 20).map(i -> "Hello!" + i));

        Single<ArrayList<String>> single = stringObservable.collect(ArrayList::new, ArrayList::add);
        Single<List<String>> listSingle = stringObservable.toList();

        single.subscribe(System.out::println);
    }


    @Test
    public void testObserveOnVsSubscribeOn() throws Exception {

        ExecutorService service = Executors.newFixedThreadPool(5);

        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                System.out.println("Inside source:" + Thread.currentThread().getName());
                for (int i = 0; i < 25; i++) {
                    e.onNext(i);
                    Thread.sleep(5);
                }
                e.onComplete();
            }
        }).cache();

        observable.subscribeOn(Schedulers.from(service))
                  .doOnNext(x -> System.out.println("Before observeOn: " + Thread.currentThread().getName()))
                  .observeOn(Schedulers.from(service))
                  .map(x -> x + 1)
                  .doOnNext(x -> System.out.println("After map: " + Thread.currentThread().getName()))
                  .subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done!"));

        observable.subscribe(System.out::println);


        Thread.currentThread().join(5000);
        System.out.println("We are done!");
        service.shutdown();
    }

    @Test
    public void testStoppingObservableAfterSubscription() throws Exception {
        Observable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS).map(x -> x + 1);

        Disposable disposable1 = longObservable.subscribe(x -> System.out.println("L1:" + x));
        longObservable.subscribe(x -> System.out.println("L2:" + x));

        Thread.sleep(2000);

        disposable1.dispose();

        longObservable.subscribe(x -> System.out.println("Last Sub:" + x));

        Thread.sleep(5000);
    }

    @Test
    public void testStoppingObservableUsingObserver() throws Exception {
        Observable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS).map(x -> x + 1);
        longObservable.subscribe(new Observer<Long>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println(aLong);
                if (aLong > 50) disposable.dispose();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });
    }

    @Test
    public void testMerge() throws Exception {
        Flowable<String> integerFlowable1 = Flowable.interval(5, TimeUnit.MILLISECONDS).map(x -> "F1:" + x).take(50).subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable2 = Flowable.interval(500, TimeUnit.MILLISECONDS).map(x -> "F2:" + x).take(50).subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable3 = Flowable.interval(1, TimeUnit.SECONDS).map(x -> "F3:" + x).take(50).subscribeOn(Schedulers.computation());

        Flowable.merge(integerFlowable1, integerFlowable2, integerFlowable3).subscribe(System.out::println);

        Thread.sleep(50000);
    }

    @Test
    public void testConcat() throws Exception {
        Flowable<String> integerFlowable1 = Flowable.interval(5, TimeUnit.MILLISECONDS)
                                                    .take(50)
                                                    .map(x -> "F1:" + x)
                                                    .subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable2 = Flowable.interval(500, TimeUnit.MILLISECONDS)
                                                    .take(50)
                                                    .map(x -> "F2:" + x)
                                                    .subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable3 = Flowable.interval(1, TimeUnit.SECONDS)
                                                    .take(50)
                                                    .map(x -> "F3:" + x)
                                                    .subscribeOn(Schedulers.computation());

        Flowable.concat(integerFlowable1, integerFlowable2, integerFlowable3).subscribe(System.out::println);

        Thread.sleep(50000);
    }

    @Test
    public void testAmb() throws Exception {
        Flowable<String> integerFlowable1 = Flowable.interval(5, TimeUnit.MILLISECONDS)
                                                    .take(50)
                                                    .map(x -> "F1:" + x)
                                                    .delay(2, TimeUnit.SECONDS)
                                                    .subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable2 = Flowable.interval(500, TimeUnit.MILLISECONDS)
                                                    .take(50)
                                                    .map(x -> "F2:" + x)
                                                    .delay(1, TimeUnit.SECONDS)
                                                    .subscribeOn(Schedulers.computation());
        Flowable<String> integerFlowable3 = Flowable
                .interval(1, TimeUnit.SECONDS)
                .take(50).map(x -> "F3:" + x)
                .delay(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation());

        Flowable.amb(Lists.newArrayList(integerFlowable1, integerFlowable2, integerFlowable3)).subscribe(System.out::println);

        Thread.sleep(50000);
    }


    @Test
    public void testWritingOurOwnFlowable() throws Exception {
//        Flowable<Long> flowable = Flowable.create(new FlowableOnSubscribe<Long>() {
//            @Override
//            public void subscribe(FlowableEmitter<Long> e) throws Exception {
//                while(e.requested() > 0) {
//
//                }
//            }
//        }, BackpressureStrategy.BUFFER);
    }



}







