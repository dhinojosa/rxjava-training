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
       subscriber.onSubscribe(new Subscription() {
           private final AtomicLong counter = new AtomicLong(0);  //50
           private final AtomicLong requests = new AtomicLong(0); //20
           private final AtomicBoolean done = new AtomicBoolean(false);
           private final AtomicBoolean started = new AtomicBoolean(false);

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
                       if (requests.decrementAndGet() >= 0){ //remove the equal?
                           subscriber.onNext(counter.getAndIncrement());
                       }
                   }
                   //subscriber.onComplete(); //Optional
                   started.set(false);
               });
           }

           @Override
           public void cancel() {
               done.set(true); //cancel at anytime
           }
       });
    }
}
