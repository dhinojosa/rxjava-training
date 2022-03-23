package com.xyzcorp.instructor;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {

    @Test
    public void testMyPublisher() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        MyPublisher myPublisher = new MyPublisher(executorService);

        myPublisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
               s.request(100);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.format("S1 (onNext): %d [%s]\n", aLong, Thread.currentThread());
            }

            @Override
            public void onError(Throwable t) {
                System.out.printf("S1 (onError): %s [%s]\n", t.getMessage(), Thread.currentThread());
            }

            @Override
            public void onComplete() {
                System.out.printf("S1 (Complete): [%s]\n", Thread.currentThread());
            }
        });

        myPublisher.subscribe(new Subscriber<>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(500);
            }

            @Override
            public void onNext(Long aLong) {
                if (aLong == 399) subscription.cancel();
                System.out.format("S2 (onNext): %d [%s]\n", aLong, Thread.currentThread());
            }

            @Override
            public void onError(Throwable t) {
                System.out.printf("S2 (onError): %s [%s]\n", t.getMessage(), Thread.currentThread());
            }

            @Override
            public void onComplete() {
                System.out.printf("S2 (Complete): [%s]\n", Thread.currentThread());
            }
        });

        Thread.sleep(10000);
    }
}
