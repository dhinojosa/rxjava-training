package com.xyzcorp;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import io.reactivex.functions.Action;
import org.junit.Test;

public class BasicObservableTest {

	@Test
	public void testBasicObservable() {
		Observable.<Integer>create(s -> {
			s.onNext(50);
			s.onNext(150);
			s.onComplete();
		}).subscribe(new Observer<Integer>() {

			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(Integer integer) {
				System.out.println(integer);
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete() {
				System.out.println("Done");
			}
		});
	}

	@Test
	public void testBasicObservableWithActions() {

		Observable<Integer> observable = Observable.<Integer>create(s -> {
			System.out.println("Inside Observable" + Thread.currentThread().getName());
			s.onNext(50);
			// s.onError(new Throwable("Oops"));
			s.onNext(150);
			s.onComplete();
		});

		observable.subscribe(x -> {
			System.out.println("Inside OnNext #1:" + Thread.currentThread().getName());
			System.out.println(x);
		}, t -> System.out.format("Got an error with message #1: %s", t.getMessage()), () -> System.out.println("Done!"));

		observable.subscribe(x -> {
			System.out.println("Inside OnNext #2:" + Thread.currentThread().getName());
			System.out.println(x);
		}, t -> System.out.format("Got an error with message #2: %s", t.getMessage()), () -> System.out.println("Done!"));
	}
	
	
	@Test
	public void testBasicObservableWithActionsAndMapAndRepeat() {
		Observable<Integer> observable = Observable.<Integer>create(s -> {
			System.out.println("Inside Observable" + Thread.currentThread().getName());
			s.onNext(50);
			// s.onError(new Throwable("Oops"));
			s.onNext(150);
			s.onComplete();
		});

		observable.map(x -> x + 10).subscribe(x -> {
			System.out.println("Inside OnNext #1:" + Thread.currentThread().getName());
			System.out.println(x);
		}, t -> System.out.format("Got an error with message #1: %s", t.getMessage()), 
				() -> System.out.println("Done!"));

		observable.subscribe(x -> {
			System.out.println("Inside OnNext #2:" + Thread.currentThread().getName());
			System.out.println(x);
		}, t -> System.out.format("Got an error with message #2: %s", t.getMessage()), () -> System.out.println("Done!"));
	}
}
