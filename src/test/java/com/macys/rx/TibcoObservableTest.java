package com.macys.rx;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import org.junit.Test;

public class TibcoObservableTest {


    //THIS IS NOT FINAL, DO MORE RESEARCH
    @Test
    public void createYourOwnTibco() throws Exception {
        Flowable<String> flowable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                while (!e.isCancelled()) {
                    long numberRecords = e.requested();
                    System.out.println(numberRecords);
                    if (numberRecords > 0) {
                    }
                }
            }
        }, BackpressureStrategy.BUFFER);


        flowable.map(x -> x + "Yay!").subscribe(System.out::println);
    }
}
