package com.xyzcorp.instructor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MyPublisher implements Publisher<Long> {

    private ExecutorService executorService;

    public MyPublisher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void subscribe(Subscriber<? super Long> s) {
        AtomicLong requests = new AtomicLong();
        AtomicLong counter = new AtomicLong();
        AtomicBoolean done = new AtomicBoolean();

        s.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                invokeLoop(n);
            }

            private void invokeLoop(long n) {
                executorService.submit(() -> {
                    requests.addAndGet(n);
                    while (!done.get() && requests.get() != 0) {
                        s.onNext(counter.incrementAndGet());
                        requests.decrementAndGet();
                    }
                    s.onComplete();
                });
            }

            @Override
            public void cancel() {
                done.set(true);
            }
        });
    }
}
