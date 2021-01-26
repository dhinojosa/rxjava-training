package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observables.GroupedObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.schedulers.Timed;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ObservableTest {
    @Test
    public void testBasicObservable() {

        //CTRL + J = Docs
        //CMD + P, Alt + P = Parameters
        //CMD + OPT + V = Introduce Variable
        //CMD + OPT + N = Inline

        Observable.<Long>create(emitter -> {
            emitter.onNext(40L);
            emitter.onNext(20L);
            emitter.onNext(80L);
            emitter.onComplete();
        }).subscribe(System.out::println);
    }

    @Test
    public void testBasicObservableWithObserver() {

        //CTRL + J = Docs
        //CMD + P, Alt + P = Parameters
        //CMD + OPT + V = Introduce Variable
        //CMD + OPT + N = Inline

        Observer<Long> longObserver = new Observer<Long>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                this.disposable = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                System.out.println(aLong);
                if (aLong > 100) disposable.dispose();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        };
        Observable
            .just(40L, 20L, 80L)
            .subscribe(longObserver);
    }

    @Test
    public void testWithConsumers() {

        Observable
            .just(40L, 20L, 80L)
            .subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    public <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s\n", label, a,
            Thread.currentThread().getName());
    }

    @Test
    public void testDoOnNext() {
        Observable
            .range(0, 10)
            .doOnNext(x -> debug("1", x))
            .map(x -> x * 10)
            .subscribe(x2 -> debug("2", x2));
    }

    @Test
    public void testFutureToObservable() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Future<String> future =
            executorService.submit(() -> "Hello from Java Future");
        Observable.fromFuture(future)
                  .map(s -> s + "!")
                  .doOnNext(s -> debug("1", s))
                  .subscribe(System.out::println);
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable
            .interval(1, TimeUnit.SECONDS)
            .doOnNext(x -> debug("in don: ", x))
            .subscribe(System.out::println);
        Thread.sleep(10000);
    }


    @Test
    public void testDefer() throws InterruptedException {
        System.out.println("1:" + LocalDateTime.now());
        Observable<LocalDateTime> observable =
            Observable.defer(() -> Observable.just(LocalDateTime.now()));
        System.out.println("2:" + LocalDateTime.now());
        Thread.sleep(10000);
        System.out.println("3:" + LocalDateTime.now());
        observable.subscribe(System.out::println);
        System.out.println("4:" + LocalDateTime.now());
        Thread.sleep(2000);
        observable.subscribe(System.out::println);
        System.out.println("5:" + LocalDateTime.now());
        Thread.sleep(1000);
    }

    @Test
    public void testForkingWithMapAndFilter() throws InterruptedException {
        Observable<Long> interval = Observable
            .interval(1, TimeUnit.SECONDS);

        interval.map(x -> x * 2)
                .doOnNext(x -> debug("fork 1", x))
                .subscribe(x -> System.out.println("Map * 2: " + x));

        interval.filter(x -> x % 2 != 0)
                .doOnNext(x -> debug("fork 2", x))
                .subscribe(x -> System.out.println("All Odds Sub: " + x));

        Thread.sleep(30000);
    }


    @Test
    public void testFlatMap() throws InterruptedException {
        Observable<Long> longObservable = Observable
            .interval(1, TimeUnit.SECONDS)
            .flatMap(x -> Observable.just(-x, x, x + 3));

        longObservable.subscribe(System.out::println);

        Thread.sleep(10000);
    }


    @Test
    public void testFlatMap2() {
        List<Employee> jkRowlingsEmployees =
            Arrays.asList(
                new Employee("Harry", "Potter", 30000),
                new Employee("Hermione", "Granger", 32000),
                new Employee("Ron", "Weasley", 32000),
                new Employee("Albus", "Dumbledore", 40000));

        Manager jkRowling =
            new Manager("J.K", "Rowling", 46000, jkRowlingsEmployees);

        List<Employee> georgeLucasEmployees =
            Arrays.asList(
                new Employee("Luke", "Skywalker", 33000),
                new Employee("Princess", "Leia", 36000),
                new Employee("Han", "Solo", 36000),
                new Employee("Lando", "Calrissian", 41000));

        Manager georgeLucas =
            new Manager("George", "Lucas", 46000, georgeLucasEmployees);


        Observable.just(georgeLucas, jkRowling)
                  .flatMap(man -> getManagerAndEmployeeObservable(man))
                  .map(Employee::getSalary)
                  .reduce(Integer::sum)
                  .subscribe(x -> System.out.println("Total Salary:" + x));
    }

    private Observable<Employee> getManagerAndEmployeeObservable(Manager man) {
        return Observable.concat(Single.just(man).toObservable(),
            Observable.fromIterable(man.getEmployees()));
    }

    @Test
    public void testFilterFromSingle() {
        Maybe<String> stringMaybe =
            Single.just("Hello").filter(s -> s.length() == 5);
        stringMaybe.subscribe(System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"));
    }

    @Test
    public void testGroupBySimple() {
        Observable<GroupedObservable<String, Integer>> groupBy =
            Observable.just(1, 2, 3, 4, 5, 6)
                      .groupBy(integer -> integer % 2 == 0 ? "Even" : "Odd");

    }

    @Test
    public void testStartWith() {
        Observable.just(1, 2, 3, 4)
                  .startWith(Observable.just(-1, 0))
                  .subscribe(System.out::println);
    }

    @Test
    public void testRepeat() {
        Observable.just(1, 2, 3).repeat(4).subscribe(System.out::println);
    }

    @Test
    public void testMerge() {
        Observable<Integer> observable1 =
            Observable.just(1, 2, 3, 4);
        Observable<Character> observable2 =
            Observable.just('a', 'b', 'c', 'd');

        Observable<? extends Serializable> merge =
            Observable.merge(observable1, observable2);

        merge.subscribe(System.out::println);
    }

    @Test
    public void testMergeAgainButDifferent() throws InterruptedException {
        Observable<String> o1 =
            Observable.interval(1, TimeUnit.SECONDS)
                      .map(i -> "O1: " + i);

        Observable<String> o2 =
            Observable.interval(5, TimeUnit.SECONDS)
                      .map(i -> "O2: " + i);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        o1.mergeWith(o2).take(100).subscribe(
            System.out::println,
            Throwable::printStackTrace,
            countDownLatch::countDown
        );

        countDownLatch.await();
    }

    @Test
    public void testMergeAgainButDifferentThreads() throws InterruptedException {
        Observable<String> o1 =
            Observable.range(0, 1000000)
                      .map(x -> "O1:" + x)
                      .subscribeOn(Schedulers.computation());

        Observable<String> o2 =
            Observable.range(0, 1000000)
                      .map(x -> "O2:" + x)
                      .subscribeOn(Schedulers.computation());

        Observable.merge(o1, o2).subscribe(System.out::println);

        Thread.sleep(1000);
    }


    @Test
    public void testConcat() throws InterruptedException {
        Observable<String> o1 =
            Observable.interval(1, TimeUnit.SECONDS)
                      .take(5)
                      .map(i -> "O1: " + i);

        Observable<String> o2 =
            Observable.interval(5, TimeUnit.SECONDS)
                      .map(i -> "O2: " + i);

        o1.concatWith(o2).subscribe(System.out::println);
        Thread.sleep(40000);
    }

    @Test
    public void testAmb() throws InterruptedException {
        Observable<Integer> o1 = Observable.range(1, 10)
                                           .delay(5, TimeUnit.SECONDS);

        Observable<Integer> o2 = Observable.range(10, 10)
                                           .delay(2, TimeUnit.SECONDS);

        Observable<Integer> o3 = Observable.range(20, 10)
                                           .delay(15, TimeUnit.SECONDS);

        Observable.amb(Arrays.asList(o1, o2, o3)).subscribe(System.out::println);

        Thread.sleep(30000);
    }

    @Test
    public void testWithObserver() {
        Observable<String> groceries = Observable.just(
            "Almonds",
            "Naan",
            "Eggs",
            "Broccoli",
            "Pineapple",
            "Potatoes");

        Observable<String> result = Observable.zip(groceries, Observable.range(1
            , 1000), (x, y) -> y + ". " + x);

        TestObserver<String> testObserver = result.test();

//        testObserver.assertValues("1. Almonds", "2. Naan", "3. Eggs",
//                                  "4. Broccoli", "5. Pineapple", "6.
//                                  Potatoes");
//

        testObserver.assertValueAt(0, s -> s.equals("1. Almonds"));
        testObserver.assertValueAt(1, s -> s.equals("2. Naan"));
        testObserver.assertValueAt(5, s -> s.equals("6. Potatoes"));
    }

    @Test
    public void testZip() throws InterruptedException {
        Observable<String> groceries = Observable.just(
            "Almonds",
            "Naan",
            "Eggs",
            "Broccoli",
            "Pineapple",
            "Potatoes");

        groceries.test();

        TestScheduler testScheduler = new TestScheduler();
        Observable<Long> interval = Observable
            .interval(1, TimeUnit.SECONDS, testScheduler);
        Observable<Long> range =
            interval.map(i -> i + 1);

        Observable<String> stringObservable =
            groceries
                .zipWith(range,
                    (s, integer) -> String.format("%d. %s", integer, s));

        TestObserver<String> testObserver = new TestObserver<>();
        stringObservable.subscribe(testObserver);

        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        testObserver.assertNoErrors();
        testObserver.assertValues(
            "1. Almonds",
            "2. Naan");

        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        testObserver.assertNoErrors();
        testObserver.assertValues(
            "1. Almonds",
            "2. Naan",
            "3. Eggs",
            "4. Broccoli");

        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        testObserver.assertNoErrors();
        testObserver.assertValues(
            "1. Almonds",
            "2. Naan",
            "3. Eggs",
            "4. Broccoli",
            "5. Pineapple",
            "6. Potatoes"
        );
    }

    @Test
    public void testReduce() {
        Maybe<Integer> result =
            Observable.range(1, 5)
                      .reduce((subtotal, next) -> subtotal + next);
        result.subscribe(System.out::println);
    }

    @Test
    public void testCollect() {
        Single<ArrayList<Integer>> collect =
            Observable.range(0, 10)
                      .collect(ArrayList::new, ArrayList::add);
        collect.subscribe(System.out::println);
    }

    @Test
    public void testGroupBy() throws InterruptedException {
        List<String> strings =
            Arrays.asList("I see trees of green",
                "Red roses too", "I see them bloom",
                "For me and you", "and I think to myself",
                "What a wonderful world");

        Disposable disposable = Observable
            .fromIterable(strings)
            .flatMap(s1 -> Observable.fromArray(s1.split(" ")))
            .map(String::toUpperCase)
            .groupBy(s -> s.substring(0, 1))
            .map(go -> go.reduce(go.getKey() + ": ",
                (composite, next) -> composite + next + ","))
            .flatMap(Single::toObservable)
            .subscribe(System.out::println);

        Thread.sleep(1000);
        disposable.dispose();
    }

    @Test
    public void testDiv0() {
        Observable.just(4, 2, 0, 3, 9)
                  .flatMap(x -> Maybe.just(x).map(y -> 10 / y).onErrorComplete().toObservable())
                  .subscribe(System.out::println);
    }



    @Test
    public void testDiv0_2() {
        Observable.just(4, 2, 0, 3, 9)
                  .flatMap(x -> x == 0? Observable.empty() : Observable.just(x))
                  .map(x -> 10 /x)
                  .subscribe(System.out::println);
    }

    @Test
    public void testObservableOfObservable() {
        Observable<Observable<Integer>> just = Observable.just(Observable.just(3),
            Observable.empty(), Observable.just(10), Observable.just(12));

        just.flatMap(o -> o).subscribe(System.out::println);
    }

    @Test
    public void testSchedulers() throws InterruptedException {
        Observable.just(1, 3, 4, 5, 10, 100)     // io
                  .doOnNext(i -> debug("L1", i))  // io
                  .observeOn(Schedulers.computation()) // Scheduler change
                  .doOnNext(i -> debug("L2", i))  // computation
                  .map(i -> i * 10)              // computation
                  .observeOn(Schedulers.io())    // Scheduler change
                  .doOnNext(i -> debug("L3", i))  // io
                  .subscribeOn(Schedulers.io())
                  .subscribe(System.out::println);
        Thread.sleep(10000);
    }
}
