package com.macys.rx;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.LocalDateTime;

public class OpenQuestionsTest {

    class MySubscriber implements org.reactivestreams.Subscriber<LocalDateTime> {

        @Override
        public void onSubscribe(Subscription s) {
            System.out.println("Got Subscription" + s);
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(LocalDateTime localDateTime) {
            System.out.println("On Next inside of thread:" + Thread.currentThread().getName());
            System.out.println(localDateTime);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();

        }

        @Override
        public void onComplete() {
            System.out.println("Done");
        }
    }

    @Test
    public void testFromCallable() throws InterruptedException {
        Flowable<LocalDateTime> localDateTimeObservable = Flowable
                .fromCallable(LocalDateTime::now).repeat(100);

        localDateTimeObservable.parallel(3).runOn(Schedulers.io()).map(x -> {
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(500);
            return x;
        }).subscribe(new Subscriber[]{new MySubscriber(), new MySubscriber(), new MySubscriber()});

        Thread.sleep(12000);

    }
}
