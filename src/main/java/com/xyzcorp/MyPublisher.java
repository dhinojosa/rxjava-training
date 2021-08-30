package com.xyzcorp;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MyPublisher implements Publisher<Long> {

    private final ExecutorService executorService;

    public MyPublisher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void subscribe(Subscriber<? super Long> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            final AtomicBoolean done = new AtomicBoolean(false);
            final AtomicBoolean started = new AtomicBoolean(false);
            final AtomicLong requests = new AtomicLong(0L);
            final AtomicLong counter = new AtomicLong(0L);

            @Override
            public void request(long n) {
                 requests.addAndGet(n);
                 if (!started.get()) {
                     performLoop();
                     started.set(true);
                 }
            }

            private void performLoop() {
                executorService.submit(() -> {
                    while (!done.get()) {
                        if (requests.decrementAndGet() >= 0) {
                            subscriber.onNext(counter.getAndIncrement());
                        }
                    }
                    subscriber.onComplete();
                });
            }

            @Override
            public void cancel() {
                done.set(true);
            }
        });
    }
}
