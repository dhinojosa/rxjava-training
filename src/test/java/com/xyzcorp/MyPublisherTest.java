package com.xyzcorp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPublisherTest {

    @Test
    public void testJDK9FlowOrReactiveStreams() throws InterruptedException {
        //CMD+P = Show Parameters
        //CMD+OPT+V = Introduce Variable
        //CMD+SHIFT+T = Toggle between production and test
        //CMD+E = Recent Files
        //Key Promoter X - Plugin Idea

        ExecutorService executorService = Executors.newFixedThreadPool(8);
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
                System.out.println("S1:" + aLong + " " + Thread.currentThread());
                if (aLong == 47) s.cancel();
            }

            @Override
            public void onError(Throwable t) {
               t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S1: Done!");
            }
        });

        myPublisher.subscribe(new Subscriber<Long>() {
            private Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(20);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("S2:" + aLong + " " + Thread.currentThread());
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (aLong == 19) s.request(50);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("S2: Done!");
            }
        });

        Thread.sleep(10000);
    }
}
