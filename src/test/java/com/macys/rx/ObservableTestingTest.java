package com.macys.rx;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("Duplicates")
public class ObservableTestingTest {

    @Test
    public void testATestObserver() throws Exception {
        TestObserver<Integer> testObserver = new TestObserver<Integer>();
        Observable.just(1, 2, 3, 4)
                  .map(x -> x + 1)
                  .filter(x -> x % 2 == 0)
                  .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertValues(2, 4);
    }

    @Test
    public void testCustomObserver() throws Exception {
        MyObserver observer = new MyObserver();
        observer.onNext("Foo");
        assertEquals(Collections.singletonList("Foo"), observer.getResultList());
    }

    @Test
    public void testATestSubscriber() throws Exception {
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Flowable.just(1, 2, 3, 4)
                .map(x -> x + 1)
                .filter(x -> x % 2 == 0)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(2, 4);
    }


    @Test
    public void testATestScheduler() throws Exception {
        TestSubscriber<Long> testSubscriber = new TestSubscriber<>();
        TestScheduler testScheduler = new TestScheduler();

        Flowable.interval(5, TimeUnit.MILLISECONDS, testScheduler)
                .map(x -> x + 1)
                .filter(x -> x % 2 == 0)
                .subscribe(testSubscriber);

        testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(2L);

        testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);

        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(2L, 4L);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(4);


    private String getInfoFromURL(String s) throws IOException {
        URL url = new URL(s);
        url.openConnection();
        InputStream inputStream = url.openStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.lines().collect(Collectors.joining("\n"));
    }

    public Observable<Tuple2<String, Double>> processStock(List<String> stocks, Function<String, String> f) {
        Observable<String> stockNames = Observable.fromIterable(stocks);
        Observable<String> urls = stockNames.map(s -> "https://finance.google.com/finance/historical?output=csv&q=" + s);
        Observable<Optional<String>> optionalObservable = urls
                .subscribeOn(Schedulers.from(executorService))
                .flatMap(s -> {
                    try {
                        return Observable.just(Optional.of(f.apply(s)));
                    } catch (Throwable t) {
                        return Observable.just(Optional.<String>empty());
                    }
                })

                .observeOn(Schedulers.computation())
                .map(opt -> opt.map(doc -> doc.split("\n")[1].split(",")[4]))
                .doOnNext(x -> System.out.println(">>>>" + x));
        return Observable.zip(stockNames, optionalObservable, (name, price) ->
                new Tuple2<>(name, Double.parseDouble(price.orElse("0.0"))));
    }


    @Test
    public void testMakingObservablesTestable() throws IOException, InterruptedException {
        String fakeData = "Date,Open,High,Low,Close,Volume\n" +
                "10-Oct-17,1097.00,1123.00,1097.00,1110.00,11978\n" +
                "9-Oct-17,1112.00,1120.00,1112.00,1120.00,5660\n" +
                "6-Oct-17,1110.00,1110.00,1104.00,1104.00,1537\n" +
                "5-Oct-17,1078.00,1105.00,1078.00,1105.00,8134\n";


        Observable<Tuple2<String, Double>> tuple2Observable = processStock(Arrays.asList("M", "ATT"), s -> fakeData);

        tuple2Observable.subscribe(x -> System.out.println("sub: " + x));

        TestObserver<Tuple2<String, Double>> testObserver = new TestObserver<>();


        tuple2Observable.subscribe(testObserver);

        Thread.sleep(1000);
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);
        testObserver.assertValueSequence(Arrays.asList(new Tuple2<>("M", 1110.0), new Tuple2<>("ATT", 1110.0)));

        Thread.sleep(1000);
    }

    @Test
    public void testMakingObservablesTestableIntegrationTest() throws IOException, InterruptedException {
        String fakeData = "Date,Open,High,Low,Close,Volume\n" +
                "10-Oct-17,1097.00,1123.00,1097.00,1110.00,11978\n" +
                "9-Oct-17,1112.00,1120.00,1112.00,1120.00,5660\n" +
                "6-Oct-17,1110.00,1110.00,1104.00,1104.00,1537\n" +
                "5-Oct-17,1078.00,1105.00,1078.00,1105.00,8134\n";


        Observable<Tuple2<String, Double>> tuple2Observable = processStock(Arrays.asList("M", "ATT"), s ->
        {
            String data = "";
            try {
                data = getInfoFromURL(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        });

        TestObserver<Tuple2<String, Double>> testObserver = new TestObserver<>();

        tuple2Observable.subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertValues(new Tuple2<>("M", 1111.0), new Tuple2<>("ATT", 1111.0));
    }

}
