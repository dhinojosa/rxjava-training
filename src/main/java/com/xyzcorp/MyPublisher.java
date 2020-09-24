package com.xyzcorp;

//JDK 8

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

//JDK 9
//import java.util.concurrent.Flow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MyPublisher implements Publisher<Long> {
    private ExecutorService executorService;

    public MyPublisher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void subscribe(Subscriber<? super Long> subscriber) {

        subscriber.onSubscribe(new Subscription() {
            final AtomicLong counter = new AtomicLong(0L);
            final AtomicBoolean done = new AtomicBoolean();

            @Override
            public void request(long n) {
                executorService.submit(() -> {
                    System.out.println("In subscribe:" + Thread.currentThread().getName());
                    if (n < 0) subscriber
                        .onError(
                            new Throwable("count request should be positive"));

                    long goal = counter.get() + n;
                    while (counter.get() < goal && !done.get()) {
                        if (counter.get() == Long.MAX_VALUE) {
                            subscriber.onComplete();
                        }
                        subscriber.onNext(counter.getAndIncrement());
                    }
                });
            }


            @Override
            public void cancel() {
                done.set(true);
            }
        });
    }
}
