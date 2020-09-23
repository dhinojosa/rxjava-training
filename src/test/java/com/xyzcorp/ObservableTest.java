package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                executorService.submit(() -> {
                    System.out.println("In future: " + Thread
                            .currentThread().getName());
                    return 68;
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
    public void testFlatMapAndGroupByWithWordSize() {

        Observable.just("I see trees of green",
                "Trees provide oxygen! Damn it!",
                "Oxygen is in water")
                  .map(String::toLowerCase)
                  .flatMap(s -> Observable.from(s.split(" ")))
                  .map(s -> s.replace("!", ""))
                  .groupBy(String::length)
                  .subscribe(go -> go.subscribe(s -> {
                      System.out.println("Group Key:" + go.getKey() + " " +
                              "Value:" + s);
                  }));
    }

    @Test
    public void testFlatMapAndGroupByWithWordCount() {
        Observable<String> sentences =
                Observable.just("I see trees of green",
                        "Trees provide oxygen! Damn it!",
                        "Oxygen is in water");

        Observable<String> words = sentences
                .map(String::toLowerCase)
                .flatMap(s -> Observable.from(s.split(" ")))
                .map(s -> s.replace("!", ""));

        Observable<GroupedObservable<String, String>>
                grouped = words.groupBy(s -> s);

        Observable<Pair<String, Integer>> pairObservable =
                grouped.flatMap(go ->
                        go.count()
                          .map(i -> new Pair<>(go.getKey(), i)));

        pairObservable.subscribe(System.out::println);
    }


    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    private Future<String> getStockPrices(String symbol) {
        return executorService.submit(() -> {
            String urlString = "https://www.google.com/finance/getprices?q="
                    + symbol + "&i=60&p=15d&f=d,o,h,l,c,v";
            InputStream is = new URL(urlString).openStream();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        });
    }

    private Observable<Long> getAverageVolume(Observable<String> data) {
        Observable<Long> volumes = data.map(s -> s.split(",")[5])
                                       .map(Long::valueOf)
                                       .onExceptionResumeNext(Observable.just
                                               (0L));
        Observable<Long> subtotalObservable = volumes.reduce(0L, (total,
                                                                  next) ->
                total + next);
        return subtotalObservable.flatMap(t -> volumes.count().map(c -> t / c));
    }


    private Observable<Long> getAverageVolume(String symbol) {
        return Observable.from(getStockPrices(symbol))
                         .flatMap(b ->
                                 getAverageVolume(Observable.from(b.split
                                         ("\n")).skip(8)));
    }

    @Test
    public void testGetStockPrices() {
        Observable<String> symbolObservable =
                Observable.just("GOOG", "M", "AMD", "NVDA");

        Observable<Pair<String, Long>> averageObservable =
                symbolObservable
                        .flatMap(symbol ->
                                getAverageVolume(symbol)
                                        .map(avg -> new Pair<>(symbol, avg)));

        averageObservable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }


    @Test
    public void testAverageVolume() {
        Observable<String> data = Observable.just(
                "1,1009.2,1011.74,1008.29,1009.93,7800",
                "2,1013.98,1013.98,1007.76,1010.27,4400",
                "3,1010.06,1013.455,1009.67,1013.455,4000"
        );

        getAverageVolume(data).subscribe(System.out::println);
    }

    @Test
    public void testFlatMapMapCombo() {
        Observable<Integer> o1 = Observable.just(1, 2, 3);
        Observable<Character> o2 = Observable.just('a', 'b', 'c');

        Observable<Pair<Integer, Character>> pairObservable = o1.flatMap(x ->
                o2.map(y -> new Pair<>(x, y)));

        pairObservable.subscribe(System.out::println);
    }

    @Test
    public void testZipCombo() throws InterruptedException {
        Observable<Integer> o1 = Observable.range(1, 10);
        Observable<Character> o2 = Observable.just('a', 'b', 'c');

        Observable<Pair<Integer, Character>> pairObservable =
                o1.zipWith(o2, Pair::new);

        Observable<Pair<Integer, Character>> pairObservable2 =
                Observable.zip(o1, o2, Pair::new);

        pairObservable.subscribe(System.out::println);
        pairObservable2.subscribe(System.out::println);

        Observable<String> pairObservable3 =
                Observable.zip(Observable.empty(), o2, (n, c) -> n + ". " + c);

        System.out.println(pairObservable3
                .toList()
                .toBlocking()
                .singleOrDefault(Collections.emptyList()));

        Thread.sleep(2000);
    }

    @Test
    public void testSchedulersObserveOn() throws InterruptedException {
        Observable
                .interval(250, TimeUnit.MILLISECONDS)
                .doOnNext(i -> printCurrentThread("Stage1", i))
                .observeOn(Schedulers.newThread())
                .map(tick -> LocalDateTime.now())
                .doOnNext(i -> printCurrentThread("Stage2", i))
                .observeOn(Schedulers.from(executorService))
                .filter(ldt -> ldt.getSecond() % 2 != 0)
                .doOnNext(i -> printCurrentThread("Stage3", i))
                .subscribe(System.out::println);

        Thread.sleep(20000);
    }

    @Test
    public void testSchedulersSubscribeOn() throws InterruptedException {
        Observable
                .range(2, 10000)
                .doOnNext(i -> printCurrentThread("Stage1", i))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(tick -> LocalDateTime.now())
                .doOnNext(i -> printCurrentThread("Stage2", i))
                .observeOn(Schedulers.from(executorService))
                .filter(ldt -> ldt.getSecond() % 2 != 0)
                .doOnNext(i -> printCurrentThread("Stage3", i))
                .subscribe(System.out::println);

        Thread.sleep(20000);
    }


    @Test
    public void testColdObservable() throws InterruptedException {
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);

        interval
                .doOnNext(i -> printCurrentThread("Before S1 Subscribe"))
                .subscribe(n -> System.out.println("S1:" + n));

        printCurrentThread("Before Sleep 2000");
        Thread.sleep(2000);

        interval
                .doOnNext(i -> printCurrentThread("Before S2 Subscribe"))
                .subscribe(n -> System.out.println("S2:" + n));

        printCurrentThread("Before Sleep 10000");
        Thread.sleep(10000);
    }

    @Test
    public void testHotObservable() throws InterruptedException {
        ConnectableObservable<Long> interval =
                Observable.interval(1, TimeUnit.SECONDS).publish();

        interval.connect();
        Thread.sleep(2000);

        interval
                .doOnNext(i -> printCurrentThread("Before S1 Subscribe"))
                .subscribe(n -> System.out.println("S1:" + n));

        Thread.sleep(2000);

        interval
                .doOnNext(i -> printCurrentThread("Before S2 Subscribe"))
                .subscribe(n -> System.out.println("S2:" + n));


        Thread.sleep(1000);
    }

    @Test
    public void testHotObservableWithRefCount() throws InterruptedException {
        Observable<Long> interval =
                Observable.interval(1, TimeUnit.SECONDS)
                          .doOnNext(n -> System.out.println("We are running with:" + n))
                          .publish().refCount();

        Subscription s1Subscribe = interval
                .doOnNext(i -> printCurrentThread("Before S1 Subscribe"))
                .subscribe(n -> System.out.println("S1:" + n));

        Thread.sleep(2000);

        Subscription s2Subscribe = interval
                .doOnNext(i -> printCurrentThread("Before S2 Subscribe"))
                .subscribe(n -> System.out.println("S2:" + n));

        Thread.sleep(2000);
        s1Subscribe.unsubscribe();
        Thread.sleep(1000);
        s2Subscribe.unsubscribe();

        Thread.sleep(10000);
    }


    @Test
    public void testBackpressure() throws InterruptedException {
        Observable
                .interval(10, TimeUnit.MICROSECONDS)
                .onBackpressureLatest()
                .observeOn(Schedulers.io())
                .subscribe(n -> {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(n);
                });

        Thread.sleep(10000);
    }
}





















