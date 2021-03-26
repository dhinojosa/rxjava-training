package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observables.GroupedObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObservableTest {
    //CTRL + N = New Item
    //CMD+SHIFT+F12 = Full Screen

    @Test
    public void testObservableTheHardWay() throws InterruptedException {

        //Publisher
        Observable<Long> longObservable =
            Observable.create(emitter -> {
                emitter.onNext(30L);
                emitter.onNext(60L);
                emitter.onComplete();
            });

        //CMD + P = Show Parameters
        longObservable.subscribe(
            new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    System.out.println("S1:" + aLong);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    System.out.println("S1: Done");
                }
            }
        );

        //CMD+OPTION+V
        Disposable disposable = longObservable.subscribe(
            aLong -> System.out.println("S2:" + aLong),
            Throwable::printStackTrace,
            () -> System.out.println("S2:Done"));

        Thread.sleep(2000);
        disposable.dispose();
    }

    @Test
    public void testObservableTheEasyWay() {
        //Inline = CMD + OPTION + N
        Disposable done =
            Observable.just(30, 50, 87)
                      .subscribe(System.out::println,
                          Throwable::printStackTrace,
                          () -> System.out.println("Done"));
    }

    @Test
    public void testObservableWithMap() {
        Observable
            .fromArray(3, 2, 10, 19)
            .doOnNext(item -> debug(item, "In Map"))
            .map(integer -> "Z:" + integer)
            .subscribe(System.out::println);
    }

    private <A> void debug(A item, String label) {
        System.out.printf("%s: %s [%s]\n", label, item, Thread.currentThread());
    }

    @Test
    public void testJavaStream() {
        List<Integer> list = Stream
            .of(1, 2, 3, 4, 5, 10, 20, 90, 1000)
            .map(x -> x * 3)
            .filter(x -> x % 2 != 0)
            .collect(Collectors.toList());
    }

    @Test
    public void testObservableWithFilter() {
        Observable<Integer> afterFilter = Observable
            .fromArray(3, 2, 10, 19)
            .doOnNext(item -> debug(item, "In Filter"))
            .filter(x -> x % 2 != 0);

        afterFilter
            .doOnNext(item -> debug(item, "In Map Z"))
            .map(integer -> "Z:" + integer)
            .subscribe(System.out::println);

        afterFilter
            .doOnNext(item -> debug(item, "In Map M"))
            .map(integer -> "M:" + integer)
            .subscribe(System.out::println);
    }

    @Test
    public void testOfWhatYouShouldLookOutFor() {
        Observable<Integer> integerObservable =
            Observable.just(3, 10, 19).flatMap(i -> Observable.just(-i, i,
                i + 1));
    }

    @Test
    public void testFlatMapWithObservableInterval() throws InterruptedException {
        Observable
            .interval(1, TimeUnit.SECONDS)
            .doOnNext(x -> debug(x, "Before Map"))
            .map(x -> "x: Boom")
            .doOnNext(x -> debug(x, "Before FlatMap"))
            .flatMap(y ->
                Observable.interval(400, TimeUnit.MILLISECONDS)
                          .map(z -> "z: Boing" + " " + y))
            .subscribe(i -> System.out.println(i));

        Thread.sleep(10000);
    }

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

    @Test
    public void testTotalSalaries() {
        Maybe<Integer> result = Observable
            .just(jkRowling, georgeLucas)
            .flatMap(manager -> Observable.fromIterable(manager.getEmployees()))
            .map(Employee::getSalary)
            .reduce(Integer::sum);
        result
            .subscribe(System.out::println);

    }

    @Test
    public void testMaybe() {
        Observable<Integer> empty = Observable.<Integer>empty();
        Maybe<Integer> integerMaybe = empty.reduce(Integer::sum);
        integerMaybe.subscribe(new MaybeObserver<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Integer integer) {
                System.out.println(integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("On complete");
            }
        });
    }

    @Test
    public void testPuzzle2GetThisToWorkWithFlatMapAndObservableEmpty() {
        //Do not use filter although you can
        //Use flatMap
        //Use try-catch
        //Use Observable.empty()
        //What I want is an Observable<Integer> reps 100/x

        Observable<Integer> observable =
            Observable
                .just(10, 40, 0, 20, 5)
                .flatMap(x -> {
                    try {
                        return Observable.just(100 / x);
                    } catch (ArithmeticException e) {
                        return Observable.empty();
                    }
                });
        observable.subscribe(System.out::println);
    }

    @Test
    public void testDefer() throws InterruptedException {
        debug(LocalDateTime.now(), "Before Observable Creation");
        Observable<LocalDateTime> observable = Observable.defer(() -> {
            LocalDateTime ldt = LocalDateTime.now();
            return Observable.just(ldt, ldt.plusSeconds(3));
        });
        debug(LocalDateTime.now(), "After Observable Creation");
        Thread.sleep(4000);

        observable.subscribe(System.out::println);
    }

    @Test
    public void testObservableWithDelay() throws InterruptedException {
        debug(LocalDateTime.now(), "Before Observable Creation");
        Observable<LocalDateTime> timeObservable =
            Observable.defer(() -> Observable.just(1).delay(4,
                TimeUnit.SECONDS).map(x -> LocalDateTime.now()));
        debug(LocalDateTime.now(), "After Observable Creation");
        timeObservable.subscribe(System.out::println);
        Thread.sleep(6000);
    }

    @Test
    public void testRange() {
        Observable.range(10, 20).subscribe(System.out::println);
    }

    @Test
    public void testGroupBy() {
        Observable<GroupedObservable<String, Integer>> groupedObservable =
            Observable.range(0, 51)
                      .groupBy(integer -> integer % 2 == 0 ? "Even" : "Odd");

        groupedObservable.subscribe(group -> {
            String key = group.getKey();
            group.subscribe(i -> System.out.printf("%s: %d\n", key, i));
        });

    }

    @Test
    public void testGroupWithFlatMapAndMap() {
        Observable<GroupedObservable<String, Integer>> groupedObservable =
            Observable.range(0, 51)
                      .groupBy(integer -> integer % 2 == 0 ? "Even" : "Odd");
        Observable<String> integerObservable =
            groupedObservable.flatMap(g ->
                g.map(i -> String.format("%s: %d", g.getKey(), i)));
        integerObservable.subscribe(System.out::println);
    }

    @Test
    public void testMergeWithTwoObservableSameThreadSameAsConcat() {
        Observable<Integer> o1 = Observable.range(0, 10);
        Observable<Integer> o2 = Observable.range(10, 10);

        //Observable<Integer> integerObservable = o1.mergeWith(o2);
        Observable<Integer> merge = Observable.merge(o1, o2);

        merge.subscribe(System.out::println);  //concat
    }

    @Test
    public void testMergeWithTwoObservableButOnDifferentThreads() throws InterruptedException {
        Observable<String> o1 = Observable.interval(1, TimeUnit.SECONDS)
                                          .map(i -> "S1:" + i);
        Observable<String> o2 = Observable.interval(1, TimeUnit.SECONDS)
                                          .map(i -> "S2:" + i)
                                          .delay(1, TimeUnit.SECONDS);

        CountDownLatch countDownLatch = new CountDownLatch(1); //synchronizer
        Observable.merge(o1, o2).take(10).subscribe(
            System.out::println,
            Throwable::printStackTrace,
            countDownLatch::countDown
        );

        countDownLatch.await(); //Thread wait until count down latch is at 0;
    }

    @Test
    public void testConcatWithTwoObservableButOnDifferentThreads() throws InterruptedException {
        Observable<String> o1 = Observable.interval(1, TimeUnit.SECONDS)
                                          .map(i -> "S1:" + i)
                                          .take(10);
        Observable<String> o2 = Observable.interval(1, TimeUnit.SECONDS)
                                          .map(i -> "S2:" + i)
                                          .delay(1, TimeUnit.SECONDS);
        CountDownLatch countDownLatch = new CountDownLatch(1); //synchronizer
        Observable.concat(o1, o2).take(100).subscribe(
            System.out::println,
            Throwable::printStackTrace,
            countDownLatch::countDown
        );

        countDownLatch.await(); //Thread wait until count down latch is at 0;
    }

    @Test
    public void testTwoObservablesUsingAmb() throws InterruptedException {
        Observable<Integer> o1 =
            Observable.range(1, 10)
                      .delay(7, TimeUnit.SECONDS);
        Observable<Integer> o2 =
            Observable.range(10, 10)
                      .delay(5, TimeUnit.SECONDS);
        Observable<Integer> o3 =
            Observable.range(20, 10)
                      .delay(3, TimeUnit.SECONDS);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Observable.amb(Arrays.asList(o1, o2, o3))
                  .subscribe(System.out::println, Throwable::printStackTrace,
                      countDownLatch::countDown);
        countDownLatch.await();
    }

    @Test
    public void testZip() {
        Observable<String> groceries = Observable.just("Almonds",
            "Eggs", "Asparagus", "Naan", "Yogurt", "Apples", "Lettuce",
            "Limes").sorted();

        Observable<String> zippedObservable = Observable.zip(groceries,
            Observable.range(1
                , 1000), (s, i) -> String.format("%d. %s", i, s));

        zippedObservable.subscribe(System.out::println,
            Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void testZipWithTestObserver() {
        Observable<String> groceries = Observable.just("Almonds",
            "Eggs", "Asparagus", "Naan", "Yogurt", "Apples", "Lettuce",
            "Limes").sorted();

        Observable<String> zippedObservable = Observable.zip(groceries,
            Observable.range(1, 1000), (s, i) -> String.format("%d. %s", i, s));

        TestObserver<String> stringTestObserver = zippedObservable.test();
        stringTestObserver.assertValueAt(0, s -> s.equals("1. Almonds"));
        stringTestObserver.assertValueAt(5, s -> s.equals("6. Limes"));
        stringTestObserver.assertValueAt(7, s -> s.equals("8. Yogurt"));
    }

    @Test
    public void testZipWithTestScheduler() {
        TestScheduler testScheduler = new TestScheduler();
        Observable<String> groceries = Observable.just("Almonds",
            "Eggs", "Asparagus", "Naan", "Yogurt", "Apples", "Lettuce",
            "Limes").sorted();

        Observable<Long> range = Observable.interval(1, TimeUnit.SECONDS,
            testScheduler).map(x -> x + 1);

        Observable<String> resultObservable = groceries.zipWith(range,
            (s, i) -> String.format("%d. %s", i, s));

        TestObserver<String> testObserver = new TestObserver<>();
        resultObservable.subscribe(testObserver);

        //Let the games begin

        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);//I am controlling
        // the clock
        testObserver.assertValuesOnly("1. Almonds", "2. Apples");

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS);//I am controlling
        // the clock
        testObserver.assertValuesOnly("1. Almonds", "2. Apples", "3. " +
            "Asparagus");
    }

    @Test
    public void testReduceWithoutASeed() {
        Maybe<Integer> factorial =
            Observable.range(1, 5)
                      .reduce((integer, integer2) -> integer * integer2);
        factorial.subscribe(System.out::println);
    }

    @Test
    public void testReduceWithoutASeedOnEmptyObservable() {
        Maybe<Integer> factorial =
            Observable.<Integer>empty()
                .reduce((integer, integer2) -> integer * integer2);
        factorial.subscribe(System.out::println, Throwable::printStackTrace,
            () -> System.out.println("Done"));
    }

    @Test
    public void testReduceWithASeed() {
        Single<Integer> factorial =
            Observable.range(1, 5)
                      .reduce(1,
                          (integer, integer2) -> integer * integer2);
        factorial.subscribe(System.out::println);
    }

    @Test
    public void testReduceWithASeedSupplier() {
        Single<Integer> factorial =
            Observable.range(1, 5)
                      .reduceWith(() -> 1,
                          (integer, integer2) -> integer * integer2);
        factorial.subscribe(System.out::println);
    }

    @Test
    public void testCollect() {
        Single<ArrayList<Integer>> listSingle =
            Observable.range(1, 10).collect(ArrayList::new, ArrayList::add);
        listSingle.subscribe(System.out::println);
    }

    @Test
    public void testLouieArmstrongTest() {
        List<String> strings =
            Arrays.asList("I see trees of green",
                "Red roses too", "I see them bloom",
                "For me and you", "and I think to myself",
                "What a wonderful world");

        //"s: 2", "f: 1";
        Observable
            .fromIterable(strings);
    }
}

