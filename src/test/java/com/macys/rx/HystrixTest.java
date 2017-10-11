package com.macys.rx;

import org.junit.Test;
import rx.Observable;
import rx.functions.Func2;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class HystrixTest {

    @Test
    public void testNewCommand() throws Exception {
        CommandHelloWorld commandHelloWorld = new CommandHelloWorld("Danno");
        String result = commandHelloWorld.execute();
        assertEquals("Hello Danno", result);
    }

    @Test
    public void testUsingFuture() throws Exception {
        CommandHelloWorld commandHelloWorld = new CommandHelloWorld("Danno");
        Future<String> result = commandHelloWorld.queue();
        Observable.from(result).map(String::length).subscribe(System.out::println);
    }

    @Test
    public void testUsingObserve() throws Exception {
        CommandHelloWorld commandHelloWorld = new CommandHelloWorld("Danno");
        Observable<String> result = commandHelloWorld.observe();
        result.map(String::length).subscribe(System.out::println);

        CommandHelloWorld paulaCommandHelloWorld = new CommandHelloWorld("Paula");
        Observable<String> result2 = paulaCommandHelloWorld.observe();

        Observable.zip(result, result2, Tuple2::new).subscribe(System.out::println);
    }
}
