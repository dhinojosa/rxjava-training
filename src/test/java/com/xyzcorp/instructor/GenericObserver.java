package com.xyzcorp.instructor;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class GenericObserver<A> implements Observer<A> {

    private String label;

    public GenericObserver(String label) {
        this.label = label;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull A a) {
        System.out.println(label + ":" + a);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println(label + ":Done");
    }
}
