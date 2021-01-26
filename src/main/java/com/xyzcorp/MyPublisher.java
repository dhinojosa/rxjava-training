package com.xyzcorp;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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
        AtomicBoolean atomicDone = new AtomicBoolean(false);
        AtomicLong counter = new AtomicLong(0L);

        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                if (n < 0)
                    subscriber.onError(
                        new Throwable("Request can't be less than 0"));
                executorService.submit(() -> {
                    long goal = counter.get() + n;
                    while (!atomicDone.get() && counter.get() < goal) {
                        subscriber.onNext(counter.getAndIncrement());
                    }
                });
            }

            @Override
            public void cancel() {
                atomicDone.set(true);
            }
        });
    }
}
