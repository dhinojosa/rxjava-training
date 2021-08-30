package com.xyzcorp.instructor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.junit.Test;

public class ObservableTest {
    @Test
    public void testBasicObservable() {

        //CTRL+J = Documentation
        //CMD+P  = Show Parameters
        //CMD+D  = Duplicate Line
        //CMD+Delete = Remove Line

        //After f, s, t -> on next
        //After f onNext, After s onNext (this is my bet)
        Observable<Long> observable = Observable.create(emitter -> {
            emitter.onNext(10L);
            emitter.onNext(20L);
            emitter.onNext(30L);
            emitter.onComplete();
        });

        observable.subscribe(System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"));


        observable.subscribe(
            new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    System.out.println(aLong);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    System.out.println(e.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("Done");
                }
            }
        );
    }
}
