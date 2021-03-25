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

    // Source (Publisher) --> Map --> Filter --> GroupBy --> Subscriber(Out)

    // Source (Publisher) --> Map --> Filter --> GroupBy --> Subscriber(Out)
    //                                       --> Zip    -->  Subscriber(Out)

    // RestFul            --> Map --> Filter --> GroupBy --> Store to DB

    // RX     |    Stream
    @Override
    public void subscribe(Subscriber<? super Long> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            AtomicLong requests = new AtomicLong(0);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean started = new AtomicBoolean(false);
            AtomicLong counter = new AtomicLong(0);

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
                    //This is looping until done
                    while (!done.get()) {
                        if ((requests.decrementAndGet()) >= 0) {
                            subscriber.onNext(counter.incrementAndGet());
                        }
                    }
                    subscriber.onComplete();
                    started.set(false);
                });
            }

            @Override
            public void cancel() {
                done.set(true);
            }
        });
    }
}
