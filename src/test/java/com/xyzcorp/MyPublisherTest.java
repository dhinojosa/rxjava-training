package com.xyzcorp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {
    @Test
    public void testPublisher() throws InterruptedException {

        System.out.println("In test:" + Thread.currentThread().getName());
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        MyPublisher myPublisher = new MyPublisher(executorService);
        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(1000);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("In s1:" + Thread.currentThread().getName());
                System.out.println(aLong);
                if (aLong > 100) s.cancel();
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });

        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(1000);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("In s2:" + Thread.currentThread().getName());
                System.out.println("2:" + aLong);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });

        Thread.sleep(40000);
    }
}
