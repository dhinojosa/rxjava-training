package com.xyzcorp;

import org.junit.Test;
import rx.Observable;
import rx.Observer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ObservableTest {

    @Test
    public void testCreateObservable() {
        Observable<Integer> observable =
                Observable.create(subscriber -> {
                    printCurrentThread();
                    subscriber.onNext(50);
                    subscriber.onNext(30);
                    subscriber.onNext(20);
                    subscriber.onNext(110);
                    subscriber.onCompleted();
                });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                printCurrentThread();
                System.out.println("1. Completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer integer) {
                printCurrentThread();
                System.out.println("1. On Next: " + integer);
            }
        });

        observable
                .map(integer -> integer * 2)
                .subscribe(x -> System.out.println("2: On Next: " + x),
                        Throwable::printStackTrace,
                        () -> System.out.println("2: Completed"));
    }

    @Test
    public void testUsingJust() {
        Observable
                .just(10, 20, 50, 40, 100)
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Completed"));
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable
                .interval(1, TimeUnit.SECONDS) //Computation Thread
                .doOnNext(n -> printCurrentThread())
                .map(x -> x * 40)
                .doOnNext(n -> printCurrentThread())
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Completed"));

        Thread.sleep(10000);
    }

    private void printCurrentThread() {
        System.out.println(Thread.currentThread().getName());
    }

    @Test
    public void testErrorHandling() {
        Observable<Object> observable =
                Observable.create(subscriber -> {
                    System.out.println("Creating Date");
                    subscriber.onNext(LocalDateTime.now());
                    System.out.println("Created Date");
                }).cache();

        observable.subscribe(x -> System.out.println("1:" + x),
                t -> System.out.println("Caught: " + t.getMessage()));
        observable.subscribe(x -> System.out.println("2:" + x),
                t -> System.out.println("Caught: " + t.getMessage()));
    }

    @Test
    public void testDefer() {
        Observable<Integer> observable = Observable.defer(() ->
                Observable.range(30, LocalDateTime.now().getSecond()));
        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    @Test
    public void testFilter() {
        Observable<String> artistLastNames =
                Observable.just("Jackson", "Prince", "Meatloaf",
                        "Gomez", "Bieber");

        artistLastNames.filter(s -> s.length() != 5)
                       .subscribe(System.out::println);
    }

    @Test
    public void testFlatMap() {
        Observable<Integer> observable =
                Observable.just(1, 2, 3)
                          .flatMap(n -> Observable.just(-n, n, n + 1));

        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
    }

    @Test
    public void testFlatMapOnJava8Streams() {
        Stream<Integer> streamStream =
                Stream.of(1, 2, 3)
                      .flatMap(n -> Stream.of(-n, n, n + 1));
    }

    @Test
    public void testFlatMapOnOptional() {
        Optional<String> isley = Optional.of("Marinn");
        Optional<String> danno = Optional.empty();

        System.out.println(isley.map(s -> s + "!").orElse("No middle name"));
        System.out.println(danno.orElse("No middle name"));

        Optional<String> s = isley.flatMap(n -> Optional.of(n + "!"));
    }

    @Test
    public void testFlatMapWithLyrics() {
        Observable<String> lyrics = Observable.just("You put right foot in",
                "You put right foot in",
                "You put left foot in",
                "And you shake it all about",
                "You do the hokey pokey",
                "Then you turn yourself around",
                "That's what is all about");

        Observable<String> stringObservable = lyrics
                .flatMap(s -> Observable.from(s.split(" ")));

        stringObservable
                .map(String::toLowerCase)
                .distinct()
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done"));
    }
}
