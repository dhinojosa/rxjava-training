package com.xyzcorp.instructor;

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
    public void subscribe(Subscriber<? super Long> s) {
        s.onSubscribe(new Subscription() {
            AtomicLong requests = new AtomicLong();
            AtomicLong counter = new AtomicLong();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean done = new AtomicBoolean();

            @Override
            public void request(long n) {
                requests.addAndGet(n);
                if (!started.get()) {
                    startLoop();
                    started.set(true);
                }
            }

            private void startLoop() {
                executorService.submit(() -> {
                    while(!done.get()) {
                        if (requests.decrementAndGet() >= 0) {
                            s.onNext(counter.incrementAndGet());
                        }
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
