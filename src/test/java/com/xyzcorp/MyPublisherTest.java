package com.xyzcorp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {

    @Test
    public void testMyPublisher() throws InterruptedException {
        //CMD+OPT+V
        ExecutorService executorService =
            Executors.newFixedThreadPool(4);
        MyPublisher myPublisher =
            new MyPublisher(executorService);
        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(10);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("S1:" + aLong + ":" + Thread.currentThread());
                if (aLong == 9) subscription.request(10000);
                else if (aLong == 4000) subscription.cancel();
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S1: Done");
            }
        });

        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(100);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("S2:" + aLong + ":" + Thread.currentThread());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S2: Done");
            }
        });

        executorService.shutdown();
        Thread.sleep(1000);
    }


}
