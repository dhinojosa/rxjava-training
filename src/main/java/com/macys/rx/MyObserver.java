package com.macys.rx;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.List;

public class MyObserver implements Observer<String> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    public void onNext(String foo) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    public <T> List<T> getResultList() {
        return null;
    }
}
