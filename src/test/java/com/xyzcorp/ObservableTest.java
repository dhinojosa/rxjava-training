package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ObservableTest {

    @Test
    public void testBasicObservable() {
        Observable<Integer> observable = Observable.create(s -> {
            int index = 0;
            List<Integer> content = Arrays.asList(30, 40, 50);
            while (!s.isUnsubscribed() && index <= content.size() - 1) {
                s.onNext(content.get(index++));
            }
            s.onCompleted();
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Done");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                System.out.println(t);
            }
        });

        observable.map(t -> t * 2).subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));

        observable.subscribe(new
                                     Subscriber<Integer>() {
                                         @Override
                                         public void onCompleted() {
                                             System.out.println("Subscriber: " +
                                                     "Done");
                                         }

                                         @Override
                                         public void onError(Throwable e) {
                                             e.printStackTrace();
                                         }

                                         @Override
                                         public void onNext(Integer integer) {
                                             if (integer.equals(40)) {
                                                 System.out.println
                                                         ("Subscriber: Going " +
                                                                 "to unsubscribe");
                                                 unsubscribe();
                                             } else {
                                                 System.out.println
                                                         ("Subscriber On " +
                                                                 "Next:" +
                                                                 integer);
                                             }
                                         }
                                     });
    }

    @Test
    public void testUsingJust() {
        Observable.just(40, 30, 10).subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    private void printCurrentThread(String label, Object... args) {
        System.out.println(label + ": " + Thread.currentThread().getName()
                + " with args:" + Arrays.toString(args));
    }


    @Test
    public void testFromUsingFuture() {

        //Java 5 and greater

        ExecutorService executorService =
                Executors.newFixedThreadPool(5);

        Future<Integer> future =
                executorService.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        System.out.println("In future: " + Thread
                                .currentThread().getName());
                        return 68;
                    }
                });

        //RX Java

        Observable
                .from(future, 3, TimeUnit.SECONDS)
                .doOnNext(i -> printCurrentThread("observable", i))
                .subscribe(System.out::println);
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                  .startWith(100L, 101L, 102L, 103L, 104L)
                  .doOnNext(i -> printCurrentThread("after interval", i))
                  .subscribe(i -> {
                      printCurrentThread("inside subscribe");
                      System.out.println(i);
                  });


        Thread.sleep(5000); //put the main thread a sleep
    }


    @Test
    public void testARegularObservableWithJust() throws InterruptedException {
        Observable<Integer> integerObservable = Observable
                .just(1, 2, 3, 4)
                .doOnSubscribe(() -> System.out.println("Subscribed!"));

        integerObservable.subscribe(x -> System.out.println("S1:" + x));

        Thread.sleep(4000);

        integerObservable.subscribe(x -> System.out.println("S2:" + x));

        Thread.sleep(2000);

        integerObservable.subscribe(x -> System.out.println("S3:" + x));

        Thread.sleep(1000);
    }


    @Test
    public void testDefer() throws InterruptedException {
        Observable<Integer> integerObservable = Observable.defer(() -> {
            int currentSecond = LocalDateTime.now().getSecond();
            return Observable.just(currentSecond, currentSecond + 1,
                    currentSecond + 2);
        }).cache();

        integerObservable.subscribe(x -> System.out.println("S1:" + x));

        Thread.sleep(4000);

        integerObservable.subscribe(x -> System.out.println("S2:" + x));

        Thread.sleep(2000);

        integerObservable.subscribe(x -> System.out.println("S3:" + x));
    }

    @Test
    public void testSameButWithNoDefer() throws InterruptedException {
        int currentSecond = LocalDateTime.now().getSecond();
        Observable<Integer> integerObservable =
                Observable
                        .just(currentSecond, currentSecond + 1, currentSecond
                                + 2);

        integerObservable.subscribe(x -> System.out.println("S1:" + x));

        Thread.sleep(4000);

        integerObservable.subscribe(x -> System.out.println("S2:" + x));
    }

    @Test
    public void testFilter() {
        Observable.range(10, 20)
                  .map(x -> x * 3)
                  .doOnNext(x -> System.out.println("Midstream:" + x))
                  .filter(x -> x % 2 == 0)
                  .subscribe(System.out::println);
    }

    @Test
    public void testFlatMap() {

        // y = f(x)
        Observable<Integer> map =
                Observable.just(3, 10, 19)
                          .flatMap(x -> Observable.just(x + 1, x, x - 1));

        map.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    @Test
    public void testFlatMapWithWords() {

        //filter

        Observable<String> sentences =
                Observable.just("I see trees of green",
                "Trees provide oxygen! Damn it!",
                "Oxygen is in water");

        Observable<String> words = sentences
                .map(s -> s.toLowerCase())
                .flatMap(s -> Observable.from(s.split(" ")))
                .map(s -> s.replace("!", ""));

        Observable<GroupedObservable<Integer, String>>
                grouped = words.groupBy(s -> s.length());

        grouped.subscribe(new Action1<GroupedObservable<Integer, String>>() {
            @Override
            public void call(GroupedObservable<Integer, String> go) {
                go.subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.println("Group Key:" + go.getKey() + " Value:" + s);
                    }
                });
            }
        });


    }
}






















