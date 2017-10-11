package com.macys.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HotVsColdObservableTest {
    ExecutorService executorService = Executors.newCachedThreadPool();

    @Test
    public void testColdObservable() throws Exception {

        Observable<Long> longObservable = Observable.interval(1,
                TimeUnit.SECONDS,
                Schedulers.from(executorService));

        longObservable.subscribe(x -> System.out.println("L1:" + x));

        Thread.sleep(2000);

        longObservable.subscribe(x -> System.out.println("L2:" + x));



        Thread.sleep(10000);
    }

    @Test
    public void testHotObservable() throws Exception {

        ConnectableObservable<Long> connectableObservable = Observable.interval(1,
                TimeUnit.SECONDS,
                Schedulers.from(executorService)).publish();

        connectableObservable.subscribe(x -> System.out.println("L1:" + x));

        Thread.sleep(2000);

        Disposable disposable = connectableObservable.connect(); //Let us begin

        connectableObservable.subscribe(x -> System.out.println("L2:" + x));

        Thread.sleep(2000);

        connectableObservable.subscribe(x -> System.out.println("L3:" + x));

        disposable.dispose();

        Thread.sleep(10000);
    }

    @Test
    public void testHotObservableWithRefCount() throws Exception {

//        Observable<Long> longObservable = Observable.create((ObservableOnSubscribe<Long>) e -> {
//            Thread t = new Thread(() -> {
//                AtomicLong al = new AtomicLong(0);
//                while(true) {
//                    System.out.println("Ready to send out: " + al.get());
//                    e.onNext(al.getAndIncrement());
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            });
//            t.start();
//        }).publish().refCount();

        Observable<Long> longObservable = Observable.interval(1, TimeUnit.SECONDS)
                                                    .doOnNext(System.out::println).publish().refCount();

        Disposable d1 = longObservable.subscribe(x -> System.out.println("L1:" + x));

        Thread.sleep(1000);

        Disposable d2 = longObservable.subscribe(x -> System.out.println("L2:" + x));

        Thread.sleep(1000);

        Disposable d3 = longObservable.subscribe(x -> System.out.println("L3:" + x));

        Thread.sleep(1000);

        d3.dispose();

        Thread.sleep(1000);

        d2.dispose();

        Thread.sleep(1000);

        d1.dispose();

        Thread.sleep(30000);
    }

}
