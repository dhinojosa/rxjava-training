package com.xyzcorp;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observables.GroupedObservable;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ObservableTest {

    //CMD_OPT_V = Introduce Variable
    //CMD_OPT_N = Inline
    //Fix It = OPT_ENTER/ALT_ENTER

    @Test
    public void testObservable() throws InterruptedException {
        Observable<Long> observable = Observable.<Long>create(emitter -> {
            emitter.onNext(4L);
            emitter.onNext(8L);
            emitter.onNext(0L);
            emitter.onNext(12L);
            emitter.onNext(5001L);
        });
        Disposable disposable = observable
            .doOnNext(i -> System.out.println("1:" + i))
            .map(aLong -> 12 / aLong)
            .doOnNext(i -> System.out.println("2:" + i))
            .onErrorResumeWith(Observable.fromArray(30L, 12L, 122L))
            .subscribe(x -> System.out.println("S1:" + x),
                Throwable::printStackTrace, //This will never be called.
                () -> System.out.println("Done"));
        Thread.sleep(4000);
    }

    @Test
    public void testInterval() throws InterruptedException {
        //dispose with interval
        Observable<Long> intervalObservable = Observable.interval(1,
            TimeUnit.SECONDS);
        Disposable disposable = intervalObservable.subscribe(
            System.out::println,
            Throwable::printStackTrace);
        Thread.sleep(10000);
        disposable.dispose(); //Stop subscribing
        Thread.sleep(10000);
    }

    @Test
    public void testFlatMap() {
        Observable<Integer> map =
            Observable.just(1, 2, 3, 4, 5)
                      .flatMap(x -> Observable.just(-x, x, x + 1));
        map.subscribe(System.out::println);
    }

    @Test
    public void testReduce() {
        Maybe<Integer> reduction =
            Observable.just(1, 2, 3, 4, 5).reduce((total, next) -> {
                System.out.printf("total: %d\tnext: %d\n", total, next);
                return total * next;
            });
        reduction.subscribe(System.out::println, Throwable::printStackTrace);

    }

    @Test
    public void quiz() {
        List<String> strings = Arrays.asList(
            "The stars are shining bright",
            "I can see them at night",
            "It is a fantastic sight");

        //List -> Observable
        //Observable with the words only ("The", "stars", "are" ...)


        Observable<GroupedObservable<Character, String>> groupedObservable =
            Observable
                .fromIterable(strings)
                .flatMap(s1 -> Observable.fromArray(s1.split(" ")))
                .map(String::toUpperCase)
                .groupBy(w -> w.charAt(0));


        Observable<Maybe<String>> map = groupedObservable
            .map(go -> {
                Maybe<String> reduce = go.reduce((acc, next) -> {
                    //System.out.printf("acc: %s,\tnext: %s\n", acc, next);
                    return acc + "," + next;
                });
                return reduce.map(s -> go.getKey() + ": " + s);
            });

        map.subscribe(m -> m.subscribe(System.out::println));
    }


    @Test
    public void testZip() {
        List<String> groceries = Arrays.asList("Bread", "Eggs",
            "Broccoli", "Spinach", "Tomatoes", "Cheese", "Yogurt", "Cabbage",
            "Mushroom");

        //Create Observable from groceries
        //Hint: zip, zipWith
        //Hint: range (i.e. Observable Range)
        //Observable<String> elements
        // 1. Bread
        // 2. Eggs
        // 3. Broccoli
        // subscribe
    }
}


