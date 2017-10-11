package com.macys.rx;

import io.reactivex.Flowable;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.parallel.ParallelFlowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class StockFinderTest {

    //google.com/finance/historical?output=csv&q=MSFT

    ExecutorService executorService = Executors.newFixedThreadPool(4);

    public Future<String> createFuture(String urlString) throws IOException {
        Callable<String> c = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getInfoFromURL(urlString);
            }
        };
        return executorService.submit(c);
    }


    @Test
    public void testStockPriceFinderFindTheLatestCloseBug() throws IOException {
        Observable<String> stockNames = Observable.just("M", "MSFT", "BTC", "T", "ORCL");
        Observable<String> urls = stockNames.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        Observable<String> contentObservable = urls.flatMap(u -> Observable.fromFuture(createFuture(u)).onErrorResumeNext((Throwable e) -> Observable.empty()));
        Observable<String> lineObservable = contentObservable.doOnNext(s -> System.out.println(">>>>" + s)).flatMap(doc ->
                Observable.fromArray(doc.split("\n")).skip(1).take(1));
        Observable<Tuple2<String, Double>> tupleObservable = stockNames.zipWith(lineObservable.map(s -> s.split(",")[4]).map(Double::parseDouble), Tuple2::new);
        tupleObservable.subscribe(System.out::println);
    }


    private String getInfoFromURL(String s) throws IOException {
        URL url = new URL(s);
        url.openConnection();
        InputStream inputStream = url.openStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.lines().collect(Collectors.joining("\n"));
    }

    @Test
    public void testStockPriceScheduler() throws IOException, InterruptedException {
        Observable<String> stockNames = Observable.just("M", "MSFT", "T", "ORCL");
        Observable<String> urls = stockNames.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        urls.doOnNext(x -> System.out.println("doOnNext-1: " + Thread.currentThread().getName()))
            .subscribeOn(Schedulers.from(executorService))
            .map(this::getInfoFromURL)
            .observeOn(Schedulers.computation())
            .doOnNext(x -> System.out.println("doOnNext-2: " + Thread.currentThread().getName()))
            .flatMap(doc -> Observable.fromArray(doc.split("\n")).skip(1).take(1)).subscribe(System.out::println);
        Thread.sleep(10000);
    }

    @Test
    public void testStockPriceSchedulerWithFlowable() throws IOException, InterruptedException {
        Flowable<String> stockNames = Flowable.just("M", "MSFT", "T", "ORCL");
        Flowable<String> urls = stockNames.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        urls.parallel(4).runOn(Schedulers.from(executorService)).map(this::getInfoFromURL).flatMap(doc ->
                Flowable.fromArray(doc.split("\n")).skip(1).take(1)).sequential().subscribe(System.out::println);
        Thread.sleep(10000);
    }


    @Test
    public void testStockPriceSchedulerWithErrorHandling() throws IOException, InterruptedException {
        Observable<String> stockNames = Observable.just("M", "MSFT", "BTC", "T", "ORCL");
        Observable<String> urls = stockNames.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        Observable<Optional<String>> optionalObservable = urls
                .subscribeOn(Schedulers.from(executorService))
                .concatMap(s -> {
                    try {
                        return Observable.just(Optional.of(this.getInfoFromURL(s)));
                    } catch (Throwable t) {
                        return Observable.just(Optional.<String>empty());
                    }
                })
                .observeOn(Schedulers.computation())
                .map(opt -> opt.map(doc -> doc.split("\n")[1].split(",")[4]));
        Observable.zip(stockNames, optionalObservable, (s, s2) -> new Tuple2<>(s, Double.parseDouble(s2.orElse("0.0"))))
                .subscribe(System.out::println, Throwable::printStackTrace);
        Thread.sleep(10000);
    }

}
