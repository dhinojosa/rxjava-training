package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observables.GroupedObservable;
import org.junit.Test;

import java.util.ArrayList;
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
    public void testCollect() {
        Single<List<Integer>> collect =
            Observable.just(1, 2, 3, 4, 5).collect(
                () -> new ArrayList<>(), //initializer
                (integers, e) -> integers.add(e)); //going be called 5 times

        collect.subscribe(System.out::println);


        //1. result = []
        //2. integers = [], e = 1 -> integers.add(1);
        //3. integers = [1], e = 2 -> integers.add(2);
        //4. integers = [1,2], e = 3 -> integers.add(3)
        //....
        //   integers = [1,2,3,4,5]


        //Maybe = 0 or 1
        //Single = 1  (Mono)
        //Observable = 0 to infinity
        //Flowable = 0 to infinity (backpressure) (Flux)
    }

    static class ThreadObserver<T> implements Observer<T> {

        private String name;

        public ThreadObserver(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            System.out.format("%s: OnSubscribe in [%s]\n", name, Thread.currentThread().getName());
        }

        @Override
        public void onNext(@NonNull T t) {
            System.out.format("%s: On Next (%s) in [%s]\n", name, t.toString(), Thread.currentThread().getName());
        }

        @Override
        public void onError(@NonNull Throwable e) {
            System.out.format("%s: On Error (%s) in [%s]\n", name, e.getMessage(), Thread.currentThread().getName());
        }

        @Override
        public void onComplete() {
            System.out.format("%s: On Complete in [%s]\n", name, Thread.currentThread().getName());
        }
    }

    @Test
    public void collect2() {
        Single<StringBuilder> collect =
            Observable.just(1, 2, 3, 4, 5)
                      .doOnEach(new ThreadObserver<>("my-monitor"))
                      .collect(StringBuilder::new, //initializer
                          (stringBuilder, integer) -> {
                              stringBuilder.append(":");
                              stringBuilder.append(integer);
                          });
        collect.map(StringBuilder::toString)
               .subscribe(System.out::println);
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

        Observable<String> stringObservables =
            Observable.fromIterable(groceries);
        Observable<Integer> numbersObservables =
            Observable.range(1, Integer.MAX_VALUE);
        stringObservables
            .zipWith(numbersObservables, (s1, s2) -> s2 + ". " + s1)
            .subscribe(System.out::println);
    }

    @Test
    public void testAmb() throws InterruptedException {
        Observable<String> o1 =
            Observable.interval(1, TimeUnit.SECONDS)
                      .map(i -> "o1:" + i)
                      .delay(2, TimeUnit.SECONDS);
        Observable<String> o2 =
            Observable.interval(1, TimeUnit.SECONDS)
                      .map(i -> "o2:" + i)
                      .delay(1, TimeUnit.SECONDS);
        Observable<String> o3 =
            Observable.interval(1, TimeUnit.SECONDS)
                      .map(i -> "o3:" + i)
                      .delay(3, TimeUnit.SECONDS);
        Observable.ambArray(o1, o2, o3).take(5).subscribe(System.out::println);
        Thread.sleep(10000);
    }
    //1. Choose either concat, (merge, mergeWith)
    //2. Prove the marble diagrams


}


