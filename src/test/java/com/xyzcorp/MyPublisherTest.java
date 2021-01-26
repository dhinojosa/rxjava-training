package com.xyzcorp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {

    // CMD_OPT_V = Intro Variable
    // CMD_SFT_T = Toggle Test/Prod

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
                System.out.println("S1:" + aLong);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S1:Done");
            }
        });

        myPublisher.subscribe(new Subscriber<Long>() {

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                this.subscription.request(1000);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("S2:" + aLong);
                if (aLong == 40) this.subscription.cancel();
                //subscription.request(20);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S2:Done");
            }
        });

        Thread.sleep(10000);
    }
}
