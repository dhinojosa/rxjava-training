package com.xyzcorp.instructor;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {

    @Test
    public void testPublisher() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        MyPublisher myPublisher = new MyPublisher(executorService);
        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                this.subscription.request(10);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println(aLong);
                if (aLong == 10L) {
                    this.subscription.request(100);
                }
                if (aLong == 60L) {
                    this.subscription.cancel();
                }
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
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                this.subscription.request(30);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("P1: " + aLong);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("P1: Done");
            }
        });

        Thread.sleep(2000);
    }
}
