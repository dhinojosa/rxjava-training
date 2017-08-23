package com.xyzcorp;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import org.junit.Test;

public class AggregateTest {

    @Test
    public void testingReduce() throws Exception {
        Maybe<Integer> reduce = Observable.range(1, 5).reduce((total, next) -> {
            System.out.format("Total: %d, Next: %d\n", total, next);
            return total * next;
        });
        System.out.println(reduce.defaultIfEmpty(-1).blockingGet());
    }
}
