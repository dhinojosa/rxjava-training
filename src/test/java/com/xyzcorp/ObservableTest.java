package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObservableTest {

    //CMD+N, Create something new
    //CMD+SHIFT+F12, Full Screen
    //CMD+D Duplicate Line
    //CMD+B = Go to Definition
    //CMD+SHIFT+Backspace = Go to the last edit point
    //CMD+OPT+N = Inline
    //CMD+OPT+V = Variable
    //OPT+Up, Opt+Down = Increase/Decrease Selection
    //CMD+OPT+M = Method
    //CTRL+J

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testCreateObservable() {
        Observable<Long> longObservable = Observable.create(emitter -> {
            emitter.onNext(10L);
            emitter.onNext(15L);
            emitter.onNext(25L);
            emitter.onNext(-1L);
            emitter.onNext(40L);
            emitter.onNext(90L);
            emitter.onComplete();
        });

        longObservable.subscribe(x -> log("S1", x),
            Throwable::printStackTrace,
            () -> System.out.println("Done"));

        longObservable.subscribe(new Observer<Long>() {
            private Disposable d;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                this.d = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                log("S2", aLong);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });
    }

    private <A> void log(String label, A x) {
        System.out.format("%s:\t%s\t[%s]\n", label, x,
            Thread.currentThread().getName());
    }

    @Test
    public void testWithJust() {
        Observable
            .just(10L, 1L, 50L)
            .doOnNext(x -> log("S1-1", x))
            .subscribe(x -> log("S1-2", x),
                Throwable::printStackTrace,
                () -> System.out.println("S1 Done"));
    }

    @Test
    public void testObservableFromNearlyEverything() {
        Observable<@NonNull Integer> integerObservable =
            Observable.fromStream(Stream.of(3, 10, 11));
        integerObservable.subscribe(i -> log("S1", i));
    }

    //I HEART SHIFT+F6
    @Test
    public void testObservableFromPublisher() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        Observable<Long> longObservable =
            Observable.fromPublisher(new MyPublisher(executorService));
        longObservable.subscribe(i -> log("S1", i));
        Thread.sleep(1000);
    }

    @Test
    public void testMap() {
        Observable
            .just(40, 10, 9)
            .map(x -> x * 10)
            .subscribe(System.out::println);
    }

    @Test
    public void testMapChangeType() {
        Observable
            .just("New Jersey", "Minnesota", "California")
            .map(String::length)
            .subscribe(System.out::println);
    }

    @Test
    public void testFilter() {
        Observable
            .just(1, 2, 4, 10)
            .filter(integer -> integer % 2 == 0)
            .subscribe(System.out::println);
    }

    /**
     * Observable.interval (1 second)
     * Map(+1)
     * Create two branches
     * - one that filters the evens
     * - one that filters the odds
     * - subscribe to each branch printing "Even: 2", "Odd: 3"
     * - feel free to use doOnNext(x -> log("M1", x))
     * - in the end put a Thread.sleep(30000);
     */

    @Test
    public void testUseObservableIntervalAndMakeTwoBranches() throws InterruptedException {
        Observable<Long> observableInterval = Observable
            .interval(1L, TimeUnit.SECONDS)
            .map(x -> x + 1) //maybe here?
            .take(30);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        //Even Branch
        Observable<String> evenObservable =
            observableInterval
                .filter(i -> i % 2 == 0)
                .map(i -> "Even:" + i)
                .doOnNext(x -> log("s1", x));


        //Odd Branch
        Observable<String> oddObservable =
            observableInterval
                .filter(i -> i % 2 != 0)
                .map(i -> "Odd:" + i)
                .doOnNext(x -> log("s2", x));

        Observable<String> mergedObservable =
            evenObservable
                .mergeWith(oddObservable); //maybe here?

        //Lab Tomorrow: Put a timestamp on the results e.g Odd:27 {2021-05-21T09:13:24}

        mergedObservable.subscribe(x -> log("final", x),
            Throwable::printStackTrace,
            countDownLatch::countDown);

        countDownLatch.await();
    }

    @Test
    public void intervalWithOutBranch() {
        Observable<String> stringObservable = Observable
            .interval(1L, TimeUnit.SECONDS)
            .map(x -> x + 1)
            .map(x -> {
                if (x % 2 == 0) {
                    return "Even:" + x;
                }
                return "Odd:" + x;
            });
    }

    @Test
    public void testFlatMap() {
        Observable<Integer> integerObservable = Observable
            .range(20, 10)
            .flatMap(x -> Observable.just(-x, x, x + 1));

        integerObservable
            .subscribe(System.out::println);
    }

    @Test
    public void testDefer() {
        Observable<String> defer1 = createDeferWithLabel("Hello!");
        Observable<String> defer2 = createDeferWithLabel("World!");
        defer1.mergeWith(defer2).subscribe(System.out::println);
    }

    private Observable<String> createDeferWithLabel(String label) {
        return Observable.defer(() -> Observable.just(label + LocalDateTime.now()));
    }

    @Test
    public void testParameterizationWithFuture() {
        Future<Integer> future1 = add10Async(4);
        Future<Integer> future2 = add10Async(5);
    }

    private Future<Integer> add10Async(int x) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        return executorService.submit(() -> x + 10);
    }

    @Test
    public void testStream() {
        Stream.of(1,2,3,4,5,7,10)
              .map(x -> x * 5)
              .filter(x -> x % 2 == 0)
              .collect(Collectors.toList());
        System.out.println("Done");
    }

    @Test
    public void testLabFlatMap2() {
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


        Observable<Manager> startHere = Observable.just(georgeLucas, jkRowling);
    }
}
