package com.xyzcorp;

import io.reactivex.*;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

public class BackPressureTest {

    Observable<Long> crazedObservable = Observable.create(new ObservableOnSubscribe<Long>() {
        long count = 0;

        @Override
        public void subscribe(ObservableEmitter<Long> e) throws Exception {
            while (true) {
                e.onNext(count);
                count++;
            }
        }
    });

    Flowable<Long> crazedFlowable = Flowable.create(new FlowableOnSubscribe<Long>() {
        long count = 0;

        @Override
        public void subscribe(FlowableEmitter<Long> e) throws Exception {
            while (true) {
                e.onNext(count);
                count++;
            }
        }
    }, BackpressureStrategy.ERROR);


    Flowable<Long> crazedFlowableMissing = Flowable.create(new FlowableOnSubscribe<Long>() {
        long count = 0;

        @Override
        public void subscribe(FlowableEmitter<Long> e) throws Exception {
            while (true) {
                e.onNext(count);
                count++;
            }
        }
    }, BackpressureStrategy.MISSING);


    Flowable<Long> crazedFlowableBuffer = Flowable.create(new FlowableOnSubscribe<Long>() {
        long count = 0;

        @Override
        public void subscribe(FlowableEmitter<Long> e) throws Exception {
            while (true) {
                e.onNext(count);
                count++;
            }
        }
    }, BackpressureStrategy.BUFFER);

    @Test
    public void testObservableBackpressure() throws Exception {
        crazedObservable
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    Thread.sleep(5);
                    System.out.format("Received %d on Thread: %s\n",
                            aLong, Thread.currentThread().getName());
                });
    }


    @Test
    public void testFlowableBackpressureAsAnBuffer() throws Exception {

        //Q: Why is it still running?
        //Q: Can we still recover from an error
        //Q: onBackpressureBuffer how

        crazedFlowableBuffer
                .onBackpressureBuffer(30000L,
                        () -> System.out.println("Help! I'm overflowed"),
                        BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    Thread.sleep(5);
                    System.out.format("Received %d on Thread: %s\n",
                            aLong, Thread.currentThread().getName());
                });
    }

    @Test
    public void testFlowableBackpressureAsAnDrop() throws Exception {

        //Q: Why is it still running?
        //Q: Can we still recover from an error
        //Q: onBackpressureBuffer how

        crazedFlowableBuffer
                .onBackpressureDrop(n -> System.out.format("Dropping %d, cause I don't care\n", n))
                .observeOn(Schedulers.newThread())

                .subscribe(aLong -> {
                    Thread.sleep(5);
                    System.out.format("Received %d on Thread: %s\n",
                            aLong, Thread.currentThread().getName());
                });
    }
}
