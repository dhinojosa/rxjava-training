package com.xyzcorp.instructor;

import com.xyzcorp.Employee;
import com.xyzcorp.Manager;
import com.xyzcorp.MyPublisher;
import com.xyzcorp.Pair;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.BiFunction;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ObservableTest {

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    public void testBasicObservable() {

        //CTRL+J = Documentation
        //CMD+P  = Show Parameters
        //CMD+D  = Duplicate Line
        //CMD+Delete = Remove Line

        //After f, s, t -> on next
        //After f onNext, After s onNext (this is my bet)
        Observable<Long> observable = Observable.create(emitter -> {
            System.out.println("ready to emit");
            emitter.onNext(10L);
            emitter.onNext(20L);
            emitter.onNext(30L);
            emitter.onComplete();
        });

        observable.subscribe(System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"));


        observable.subscribe(
            new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    System.out.println(aLong);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    System.out.println(e.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("Done");
                }
            }
        );
    }

    @Test
    public void testJust() {
        Observable<Long> longObservable =
            Observable.just(10L, 20L, 30L);
    }

    @Test
    public void testMyPublisher() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Observable<Long> longObservable =
            Observable.fromPublisher(new MyPublisher(executorService));

        Disposable disposable =
            longObservable.subscribe(System.out::println);

        Thread.sleep(2000);
        disposable.dispose();
    }


    @Test
    public void testRangeAndDebug() {
        Observable
            .range(1, 10)
            .doOnNext(integer -> ObservableTest.this.debug("A", integer))
            .map(i -> i * 2)
            .doOnEach(new GenericObserver<>("B"))
            .subscribe(System.out::println);
    }

    private <A> void debug(String marker, A a) {
        System.out.printf("%s [%s]: %s\n", marker,
            Thread.currentThread(), a);
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                  .doOnNext(x -> debug("A", x))
                  .subscribe(System.out::println);
        System.out.println(Thread.currentThread()); //[main]
        Thread.sleep(10000);
    }

    @Test
    public void testMap() {
        Observable
            .range(1, 10)
            .map(i -> String.valueOf(i * 3))
            .subscribe(System.out::println);
    }

    @Test
    public void testFilter() {
        Observable
            .range(1, 10)
            .filter(integer -> integer % 3 == 0)
            .subscribe(System.out::println);
    }

    //Lab:
    //1. Create an interval Observable, every 1 second
    //2. Using the Observable, create two forks:
    //   a. One fork, map each element by multiplying by 2, and subscribe
    //   b. One fork, filter each element by keeping all the odd numbers, and
    //   subscribe
    //3. Thread.sleep(30000);


    @Test
    public void testIntervalLab() throws InterruptedException {
        Observable<Long> original = Observable
            .interval(1, TimeUnit.SECONDS)
            .doOnNext(x -> debug("Root", x));
        original
            .map(x -> x * 2)
            .doOnNext(x -> debug("A", x))
            .subscribe(x -> System.out.println("SA:" + x));
        Observable<Long> oddsObservable =
            original
                .filter(x -> x % 2 != 0)
                .doOnNext(x -> debug("B", x));
        oddsObservable
            .subscribe(x -> System.out.println("SB:" + x));
        oddsObservable
            .map(x -> x * 3)
            .doOnNext(x -> debug("C", x))
            .subscribe(x -> System.out.println("SC:" + x));
        Thread.sleep(10000);
    }

    @Test
    public void testFlatMap() {
        Observable.range(1, 10)
                  .flatMap(x -> Observable.just(-x, x, x + 1))
                  .subscribe(System.out::println);
    }


    public Future<Integer> callWebServiceTemperatureFor(String city) {
        return executorService.submit(() -> {
            if (city.endsWith("FL")) return 80;
            else if (city.endsWith("MT")) return 30;
            else if (city.endsWith("TX")) return 90;
            else if (city.endsWith("AZ")) return 110;
            else if (city.endsWith("CA")) return 95;
            else if (city.endsWith("WY")) return 80;
            else return 60;
        });
    }

    @Test
    public void testFlatMapCompositionally() {
        Observable<String> cities =
            Observable.just("Naples, FL", "San Diego, CA", "Billings, MT");
        Observable<String> temperatureObservable =
            cities.flatMap(city -> Observable.fromFuture(callWebServiceTemperatureFor(city))
                                             .map(i -> city + ":" + i));
        temperatureObservable.subscribe(System.out::println);
    }


    //Supplier: () -> Food
    //Function: x -> y
    //BiFunction: (x1, x2) -> y
    //Predicate: x -> (true|false)
    //Consumer: x -> ()

    @Test
    public void testDefer() throws InterruptedException {
        Observable<LocalDateTime> deferred =
            Observable.defer(() -> Observable.just(LocalDateTime.now()));

        Disposable disposable =
            Observable
                .interval(1, TimeUnit.SECONDS)
                .flatMap(x -> Observable.just(LocalDateTime.now()))
                .subscribe(System.out::println);

        Thread.sleep(40000);
    }

    @Test
    public void testConcatV1() {
        Observable<Integer> range1 = Observable.range(1, 10);
        Observable<Integer> range2 = Observable.range(100, 100);//Wrong

        Observable<Integer> jointObservable = range1.concatWith(range2);
        jointObservable.subscribe(System.out::println);
    }

    @Test
    public void testConcatV2() {
        Observable<Integer> range1 = Observable.range(1, 10);
        Observable<Integer> range2 = Observable.range(100, 100);

        Observable<Integer> jointObservable = Observable.concat(range1, range2);
        jointObservable.subscribe(System.out::println);
    }

    @Test
    public void testMergeSynchronously() {
        Observable<Integer> range1 = Observable.range(1, 10);
        Observable<Integer> range2 = Observable.range(100, 100);

        Observable<Integer> jointObservable = Observable.merge(range1, range2);
        jointObservable.subscribe(System.out::println);
    }


    @Test
    public void testMergeAsynchronously() throws InterruptedException {
        Observable<String> range1 =
            Observable.interval(1, TimeUnit.SECONDS).map(i -> "A" + i);
        Observable<String> range2 =
            Observable.interval(2, TimeUnit.SECONDS).map(i -> "B" + i);

        Observable<String> jointObservable = Observable.merge(range1, range2);
        jointObservable.subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    public void testZip() {
        Observable<Integer> observable1 = Observable.range(1, 100);
        Observable<Character> observable2 = Observable.just('a', 'b', 'c', 'd');

        Disposable disposable =
            observable1.zipWith(observable2, Pair::new)
                       .subscribe(p -> System.out.println(p.getA() + "-" + p.getB()));

//        JDK 10+
//        observable1.zipWith(observable2, (integer, character) -> new Object() {
//                Integer left = integer;
//                Character right = character;
//            }).map(o -> o.left + "-" + o.right)
//                   .subscribe(System.out::println);
    }

    @Test
    public void testReduceToSingle() {
        Single<Integer> integerSingle = Observable.range(1, 5).reduce(0, Integer::sum);
        integerSingle.subscribe(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void testReduceToMaybe() {
        Maybe<Integer> integerSingle = Observable.range(1, 5).reduce(Integer::sum);
        integerSingle.subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
    }


    @Test
    public void testReduceToMaybeWithEmptyObservable() {
        Maybe<Integer> integerSingle = Observable.<Integer>empty().reduce(Integer::sum);
        integerSingle.subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("Done"));
    }

    @Test
    public void testCollect() {
        Single<ArrayList<Integer>> collection =
            Observable.range(1, 10).collect(ArrayList::new, ArrayList::add);

        collection.subscribe(System.out::println);
    }


    //Challenge. Find me to total salary of _all_ employees,
    //only using the manager Observable
    @Test
    public void testLabEmployees() {
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











