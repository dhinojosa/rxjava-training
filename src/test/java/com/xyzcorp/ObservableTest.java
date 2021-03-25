package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
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
            Observable.just(10, 40, 0, 20, 5)
                      .flatMap(x -> {
                          try {
                              return Observable.just(100 / x);
                          } catch (ArithmeticException e) {
                              return Observable.empty();
                          }
                      });
        observable.subscribe(System.out::println);
    }
}
