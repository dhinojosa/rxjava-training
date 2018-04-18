package com.xyzcorp;


import rx.Observable;

import java.util.concurrent.TimeUnit;

public class IntervalRunner {
    public static void main(String[] args) throws InterruptedException {
       Observable.interval(1, TimeUnit.SECONDS)
                  .subscribe(System.out::println);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
