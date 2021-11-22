package com.xyzcorp.instructor;

import com.xyzcorp.Employee;
import com.xyzcorp.Manager;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ObservableTest {
    @Test
    public void testBasicObservable() {
        //CMD_OPT_N = Inline
        //CMD+OPT+V = Introduce Variable

        Observable<Long> longObservable = Observable.<Long>create(emitter -> {
            Runnable runnable = () -> {
                emitter.onNext(10L);
                emitter.onNext(40L);
                emitter.onNext(50L);
                emitter.onComplete();
            };
            new Thread(runnable).start();
        });

        longObservable.subscribe(x -> debug("S1", x));
        longObservable.subscribe(x -> debug("S2", x));
    }

    public <A> void debug(String label, A a) {
        System.out.printf("%s: %s - %s\n", label, a, Thread.currentThread());
    }

    @Test
    public void testObservableJust() throws InterruptedException {
        Observable<String> afterScheduler =
            Observable.just(10, 50, 40)
                      .subscribeOn(Schedulers.computation())
                      .map(x -> x * 5)
                      .subscribeOn(Schedulers.io()) //ignored
                      .map(String::valueOf);
        afterScheduler.subscribe(x -> debug("S1", x));
        afterScheduler.subscribe(x -> debug("S2", x));
        Thread.sleep(1000);
    }

    @Test
    public void testDifferentWaysToCreateObservableOrFlowable() {

        //Futures are not lazy
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Observable<Integer> integerObservable =
            Observable.fromFuture(executorService.submit(() -> 40 * 5));
    }

    @Test
    public void testLazinessWithCallable() {
        Observable<Long> observable =
            Observable.fromCallable(() -> {
                System.out.println("We are ready to calculate");
                return 50 * 10L;
            });

        System.out.println("This should be the first message");

        observable.subscribe(x -> debug("S1", x));
    }

    @Test
    public void testSingleShouldWorkSameWay() {
        Single<Long> singleObservable =
            Single.fromCallable(() -> {
                System.out.println("We are ready to calculate");
                return 50 * 10L;
            });

        System.out.println("This should be the first message");

        singleObservable
            .subscribe(x -> debug("S1", x));
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                  .doOnNext(i -> debug("L1", i))
                  .map(i -> i * 3)
                  .doOnNext(i -> debug("L2", i))
                  .subscribe(i -> debug("On Next:", i));
        Thread.sleep(10000);
    }

    @Test
    public void testDefer() throws InterruptedException {
        Single<LocalDateTime> localDateTimeSingle =
           Single.defer(() -> Single.just(LocalDateTime.now())); //fresh

        localDateTimeSingle.subscribe(ldt -> debug("S1", ldt));
        Thread.sleep(1000);
        localDateTimeSingle.subscribe(ldt -> debug("S2", ldt));
        Thread.sleep(1000);
        localDateTimeSingle.subscribe(ldt -> debug("S3", ldt));
        Thread.sleep(1000);
    }

    @Test
    public void testChallengeNumberOne() {
        //Create Observable with interval every 1 second
        //Create from the interval, one fork that maps the interval times 2,
        //  subscribe and print result
        //Create from the interval, another fork that will filter all the odds
        //  subscribe and print result
        //Use doOnNext or any of the doOn.... as needed.
        //You will have to use Thread.sleep(20000) towards the end.
    }

    @Test
    public void testFlatMap() throws InterruptedException {
        Observable<Long> mergeMap =
            Observable.interval(1, TimeUnit.SECONDS).take(5).flatMap(x -> Observable.just(-x, x, x + 1));

        Observable<Long> concatMap =
            Observable.interval(1, TimeUnit.SECONDS).take(5).concatMap(x -> Observable.just(-x, x,
                x + 1));

        mergeMap.subscribe();
        System.out.println("-------");
        concatMap.subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    public void testMerge() throws InterruptedException {
        Observable<String> o1 =
            Observable.interval(1, TimeUnit.SECONDS).map(i -> "S1" + i);

        Observable<String> o2 =
            Observable.interval(1, TimeUnit.SECONDS).map(i -> "S2" + i);

        o1.mergeWith(o2).subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    public void testConcat() throws InterruptedException {
        Observable<String> o1 =
            Observable.interval(1, TimeUnit.SECONDS).map(i -> "S1: " + i).take(4);

        Observable<String> o2 =
            Observable.interval(1, TimeUnit.SECONDS).map(i -> "S2: " + i);

        o1.concatWith(o2).subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    public void testReduce() {
        Single<Integer> reduce =
            Observable.range(1, 5)
                      .reduce(1, (subtotal, next) -> {
                          System.out.printf("subtotal: %d, next: %d\n", subtotal, next);
                          return subtotal * next;
                      });
        reduce.subscribe(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void testReduceWithMaybe() {
        Maybe<Integer> reduce =
            Observable.<Integer>empty()
                      .reduce((subtotal, next) -> {
                          System.out.printf("subtotal: %d, next: %d\n", subtotal, next);
                          return subtotal * next;
                      });
        reduce.subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Nothing to report"));
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


        Observable<Manager> managerObservable = Observable.just(georgeLucas, jkRowling);
    }
}




