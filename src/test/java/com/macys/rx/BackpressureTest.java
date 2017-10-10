package com.macys.rx;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

public class BackpressureTest {

    Observable crazyObservable = Observable.create(e -> {
        int count = 0;
        while (true) {
            e.onNext(count++);
        }
    });

    Flowable crazyFlowable = Flowable.create(e -> {
        int count = 0;
        while (true) {
            e.onNext(count++);
        }
    }, BackpressureStrategy.ERROR);


    Flowable crazyFlowableWithBuffer = Flowable.create(e -> {
        int count = 0;
        while (true) {
            e.onNext(count++);
        }
    }, BackpressureStrategy.BUFFER);


    Flowable crazyFlowableWithBufferWithLargeData = Flowable.create(e -> {
        int count = 0;
        while (true) {
            char[] array = new char[1000000];
            e.onNext("" + String.copyValueOf(array));
        }
    }, BackpressureStrategy.BUFFER);


    Flowable crazyFlowableWithDrop = Flowable.create(e -> {
        int count = 0;
        while (true) {
            e.onNext(count++);
        }
    }, BackpressureStrategy.DROP);

    Flowable crazyFlowableWithLatest = Flowable.create(e -> {
        int count = 0;
        while (true) {
            e.onNext(count++);
        }
    }, BackpressureStrategy.LATEST);

    @Test
    public void testSlowSubscriberFromObservable() throws Exception {
        crazyObservable.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithError() throws Exception {
        crazyFlowable.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithBuffer() throws Exception {
        crazyFlowableWithBuffer.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithBufferWithExtendedBuffer() throws Exception {
        crazyFlowableWithBuffer.observeOn(Schedulers.computation()).onBackpressureBuffer(4000).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithDrop() throws Exception {
        crazyFlowableWithDrop.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithLatest() throws Exception {
        crazyFlowableWithLatest.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

    @Test
    public void testSlowSubscriberFromFlowableWithBufferWithLargeDataChunks() throws Exception {
        crazyFlowableWithBufferWithLargeData.observeOn(Schedulers.computation()).subscribe(i -> {
            Thread.sleep(5);
            System.out.println(i);
        });
    }

}
