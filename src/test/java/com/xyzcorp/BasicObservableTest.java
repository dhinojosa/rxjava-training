package com.xyzcorp;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicObservableTest {

    @Test
    public void testBasicObservable() {
        Observable.<Integer>create(s -> {
            s.onNext(50);
            s.onNext(150);
            s.onComplete();
        }).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
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
    public void testBasicObservableWithActions() {

        Observable<Integer> observable = Observable.<Integer>create(s -> {
            System.out.println("Inside Observable" + Thread.currentThread().getName());
            s.onNext(50);
            // s.onError(new Throwable("Oops"));
            s.onNext(150);
            s.onComplete();
        });

        observable.subscribe(x -> {
                    System.out.println("Inside OnNext #1:" + Thread.currentThread().getName());
                    System.out.println(x);
                },
                t -> System.out.format("Got an error with message #1: %s",
                        t.getMessage()), () -> System.out.println("Done!"));

        observable.subscribe(x -> {
            System.out.println("Inside OnNext #2:" + Thread.currentThread().getName());
            System.out.println(x);
        }, t -> System.out.format("Got an error with message #2: %s", t.getMessage()), () -> System.out.println("Done!"));
    }


    @Test
    public void testBasicObservableWithActionsAndMapAndRepeat() {
        Observable<Integer> observable = Observable.<Integer>create(s -> {
            System.out.println("Inside Observable" + Thread.currentThread().getName());
            s.onNext(50);
            // s.onError(new Throwable("Oops"));
            s.onNext(150);
            s.onComplete();
        });

        observable.map(x -> x + 10).subscribe(x -> {
                    System.out.println("Inside OnNext #1:" + Thread.currentThread().getName());
                    System.out.println(x);
                }, t -> System.out.format("Got an error with message #1: %s", t.getMessage()),
                () -> System.out.println("Done!"));

        observable.repeat(5).subscribe(x -> {
                    System.out.println("Inside OnNext #2:" + Thread.currentThread().getName());
                    System.out.println(x);
                }, t -> System.out.format("Got an error with message #2: %s", t.getMessage()),
                () -> System.out.println("Done!"));
    }

    @Test
    public void testVerySimplified() {
        Observable.just(50, 100).subscribe(System.out::println);
    }

    @Test
    public void testWithInterval() throws InterruptedException {
        //java.util.concurrent.TimeUnit
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                  .doOnNext(i -> System.out.println(Thread.currentThread().getName()))
                  .subscribe(System.out::println);
        Thread.sleep(20000);
    }

    @Test
    public void testRange() {
        Observable.range(0, 100)
                  .filter(integer -> integer % 2 == 0)
                  .subscribe(System.out::println);
    }

    @Test
    public void testFuture() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Future<Integer> future = executorService.submit(() -> {
            System.out.println(Thread.currentThread().getName());
            Thread.currentThread().sleep(5000);
            return 50 + 900;
        });

        Observable
                .fromFuture(future)
                .subscribeOn(Schedulers.newThread()) //subscribe on
                .doOnNext(x -> System.out.println(Thread.currentThread().getName()))
                .map(x -> "yay!" + x)
                .subscribe(System.out::println);

        System.out.println("Continuing on");
        Thread.sleep(40000);
    }


    @Test
    public void testDefer() throws InterruptedException {
        Observable<LocalDateTime> deferredObservable =
                Observable.defer(() -> Observable.just(LocalDateTime.now()));

        deferredObservable.map(ldt -> ldt.plusHours(12)).subscribe(System.out::println);

        Thread.sleep(5000);

        deferredObservable.subscribe(System.out::println);
    }

    @Test
    public void testFlatMap() throws Exception {
        Observable<Integer> integerObservable =
                Observable.just(1, 2, 3);

        Observable<Integer> observable = integerObservable
                .flatMap(x -> Observable.just(-x, x, x + 1));

        Observable<Observable<Integer>> unflattenedMap = integerObservable
                .map(x -> Observable.just(-x, x, x + 1));

        observable.subscribe(System.out::println);

        Single<List<Integer>> actual = observable.toList();
        List<Integer> integers = actual.blockingGet();
        assertThat(integers).hasSize(9);

        List<List<Integer>> lists = unflattenedMap
                .map(x -> x.toList().blockingGet()).toList().blockingGet();
        System.out.println(lists);
        List<Integer> integers1 = Arrays.asList(-2, 2, 3);
        assertThat(lists).contains(integers1);
    }

    public Observable<String> getDataFor(String stockSymbol) throws Exception {
        //http://www.google.com/finance/historical?q=M&output=csv
        System.out.println("Making a web call for " + stockSymbol);
        URL url = new URL
                ("https://www.google.com/finance/historical?q="
                        + stockSymbol + "&output=csv");
        InputStream inputStream = url.openConnection().getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        return Observable.create(e -> {

            bufferedReader.lines().forEach(value -> {
                e.onNext(value);
                //  System.out.println("Got a line" + value);
            });
            e.onComplete();
        });
    }

    public void log(String stmt) {

    }

    @Test
    public void testRealLifeFlatMap() throws Exception {
        System.out.println("stockSymbols");
        Observable<String> stockSymbols = Observable.just("GOOG", "M",
                "AAPL", "MAC", "ORCL");

        System.out.println("rawData");
        Observable<String> rawData = stockSymbols
                .flatMap(ss -> getDataFor(ss).skip(1).take(1)).cache();

        System.out.println("zip");
        Observable<Tuple<String, String>> tupleObservable =
                stockSymbols.zipWith(rawData, (s, s2) -> new Tuple<>(s, s2));


        Disposable disposable = tupleObservable.doOnNext(s -> log(s.toString()))
                                         .subscribe(System.out::println,
                                                 Throwable::printStackTrace,
                                                 () -> System.out.println("Done"));


        disposable.dispose();

        System.out.println("-----------");

        tupleObservable.doOnNext(t -> System.out.format("We are process tuple 2: %s\n", t))
                       .subscribe(System.out::println,
                               Throwable::printStackTrace,
                               () -> System.out.println("Done"));


        System.out.println("-----------");

        tupleObservable.doOnNext(t -> System.out.format("We are process tuple 3: %s\n", t))
                       .subscribe(System.out::println,
                               Throwable::printStackTrace,
                               () -> System.out.println("Done"));

//        Thread.sleep(15000);


        Observable<Tuple<String, String>> map1 = stockSymbols
                .map(ss -> new Tuple<>(ss, getDataFor(ss)))
                .map(to -> new Tuple<>(to.getFirst(), to.getSecond().skip(1).take(1).blockingSingle()));

         map1.subscribe(System.out::println);
    }


}
