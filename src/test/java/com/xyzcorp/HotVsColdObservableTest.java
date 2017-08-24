package com.xyzcorp;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HotVsColdObservableTest {

    @Test
    public void testColdObservable() throws Exception {
     	Observable<String> observable1 = Observable.interval(1, TimeUnit.SECONDS).map(x -> "Player1: " + x);
        
        observable1.subscribe(System.out::println, 
        		                 Throwable::printStackTrace,
        		                 () -> System.out.println("Done"));
        
        Thread.sleep(5000);
        
        observable1.subscribe(System.out::println, 
                Throwable::printStackTrace,
                () -> System.out.println("Done"));
 
        Thread.sleep(10000);
    }
    
    @Test
    public void testHotObservable() throws Exception {
        Observable<String> observable1 = Observable.interval(1, TimeUnit.SECONDS)
                                                          .map(x -> "Player: " + x).publish().autoConnect(2);

        observable1.subscribe(System.out::println, 
        		                 Throwable::printStackTrace,
        		                 () -> System.out.println("Done"));
        
        Thread.sleep(5000);
        
        observable1.subscribe(System.out::println, 
        		Throwable::printStackTrace,
        		() -> System.out.println("Done"));
        
        Thread.sleep(5000);

        observable1.subscribe(System.out::println, 
                Throwable::printStackTrace,
                () -> System.out.println("Done"));


        Thread.sleep(10000);
    }
}
