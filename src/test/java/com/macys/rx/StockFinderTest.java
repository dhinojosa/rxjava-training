package com.macys.rx;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
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
                URL url = new URL(urlString);
                url.openConnection();
                InputStream inputStream = url.openStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                return bufferedReader.lines().collect(Collectors.joining("\n"));
            }
        };
        return executorService.submit(c);
    }


    @Test
    public void testStockPriceFinderFindTheLatestClose() throws IOException {
        Observable<String> stockPrices = Observable.just("M", "MSFT", "T", "ORCL");
        Observable<String> urls = stockPrices.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        Observable<String> contentObservable = urls.flatMap(u -> Observable.fromFuture(createFuture(u)));
        Observable<String> lineObservable = contentObservable.flatMap(doc ->
                Observable.fromArray(doc.split("\n")).skip(1).take(1));
        Observable<Double> latestStockValues = lineObservable.map(s -> s.split(",")[4]).map(Double::parseDouble);
        stockPrices.zipWith(latestStockValues, Tuple2::new).subscribe(System.out::println);
    }
}
