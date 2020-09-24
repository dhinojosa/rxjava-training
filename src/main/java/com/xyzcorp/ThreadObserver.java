package com.xyzcorp;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class ThreadObserver<T> implements Observer<T> {

    private String name;

    public ThreadObserver(String name) {
        this.name = name;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        System.out.format("%s: OnSubscribe in [%s]\n", name,
            Thread.currentThread().getName());
    }

    @Override
    public void onNext(@NonNull T t) {
        System.out.format("%s: On Next (%s) in [%s]\n", name,
            t.toString(), Thread.currentThread().getName());
    }

    @Override
    public void onError(@NonNull Throwable e) {
        System.out.format("%s: On Error (%s) in [%s]\n", name,
            e.getMessage(), Thread.currentThread().getName());
    }

    @Override
    public void onComplete() {
        System.out.format("%s: On Complete in [%s]\n", name,
            Thread.currentThread().getName());
    }
}

