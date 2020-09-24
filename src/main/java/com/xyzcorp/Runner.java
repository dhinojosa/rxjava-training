package com.xyzcorp;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
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
                s.request(10);
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

        Thread.sleep(13000);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }
}
