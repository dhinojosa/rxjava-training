package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;

import java.io.PrintStream;

public class ObservableTest {

    //CMD+N, Create something new
    //CMD+SHIFT+F12, Full Screen
    //CMD+D Duplicate Line
    //CMD+B = Go to Definition
    //CMD+SHIFT+Backspace = Go to the last edit point
    //CMD+OPT+N = Inline
    //CMD+OPT+V = Variable
    //OPT+Up, Opt+Down = Increase/Decrease Selection
    //CMD+OPT+M = Method
    //CTRL+J

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testCreateObservable() {
        Observable<Long> longObservable = Observable.create(emitter -> {
            emitter.onNext(10L);
            emitter.onNext(15L);
            emitter.onNext(25L);
            emitter.onNext(-1L);
            emitter.onNext(40L);
            emitter.onNext(90L);
            emitter.onComplete();
        });

        longObservable.subscribe(x -> log("S1", x),
            Throwable::printStackTrace,
            () -> System.out.println("Done"));

        longObservable.subscribe(new Observer<Long>() {
            private Disposable d;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                this.d = d;
            }

            @Override
            public void onNext(@NonNull Long aLong) {
               log("S2", aLong);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done");
            }
        });
    }

    private PrintStream log(String label, Long x) {
        return System.out.format("%s:\t%s\t[%s]\n", label, x, Thread.currentThread());
    }

}
