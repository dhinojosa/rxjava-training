package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import org.junit.Test;

public class ObservableTest {
    //CTRL + N = New Item
    //CMD+SHIFT+F12 = Full Screen

    @Test
    public void testObservableTheHardWay() throws InterruptedException {

        //Publisher
        Observable<Long> longObservable =
            Observable.create(emitter -> {
                emitter.onNext(30L);
                emitter.onNext(60L);
                emitter.onComplete();
            });

        //CMD + P = Show Parameters
        longObservable.subscribe(
            new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    System.out.println("S1:" + aLong);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    System.out.println("S1: Done");
                }
            }
        );

        //CMD+OPTION+V
        Disposable disposable = longObservable.subscribe(
            aLong -> System.out.println("S2:" + aLong),
            Throwable::printStackTrace,
            () -> System.out.println("S2:Done"));

        Thread.sleep(2000);
        disposable.dispose();
    }

    @Test
    public void testObservableTheEasyWay() {
        //Inline = CMD + OPTION + N
        Disposable done =
            Observable.just(30, 50, 87)
                      .subscribe(System.out::println,
                          Throwable::printStackTrace,
                          () -> System.out.println("Done"));
    }

    @Test
    public void testObservableWithMap() {
        Observable.fromArray(3, 2, 10, 19)
                  .map(new Function<Integer, String>() {
                      @Override
                      public String apply(Integer integer) throws Throwable {
                          return "Z:" + integer;
                      }
                  }).subscribe(System.out::println);
    }
}
