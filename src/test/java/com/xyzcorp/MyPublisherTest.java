package com.xyzcorp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {
    @Test
    public void testSimplePublisherWithSubscriber() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        MyPublisher myPublisher = new MyPublisher(executorService);

        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                this.subscription.request(50);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.printf("On Next (%s) [%s]: %d\n", "S1", Thread.currentThread(), aLong);
                if (aLong == 48L) subscription.request(400);
                if (aLong == 200L) subscription.cancel();
            }

            @Override
            public void onError(Throwable t) {
                System.out.printf("On Error (%s) [%s]: %s\n", "S1", Thread.currentThread(), t.getMessage());

            }

            @Override
            public void onComplete() {
                System.out.printf("On Complete (%s) [%s]\n", "S1", Thread.currentThread());
            }
        });

        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                this.subscription.request(100);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("On Next (S2):" + aLong);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("On Error (S2):" + t.getMessage());

            }

            @Override
            public void onComplete() {
                System.out.println("On Complete (S2)");
            }
        });


        Thread.sleep(10000);
    }
}
