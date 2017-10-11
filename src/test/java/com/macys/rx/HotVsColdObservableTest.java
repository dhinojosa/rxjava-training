package com.macys.rx;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


}
