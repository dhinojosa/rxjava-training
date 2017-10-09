package com.macys.rx;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

public class LambdasTest {

	public int process(Function<String, Integer> f, int z) {
	    return f.apply("Foo") + z;	
	}
	
	@Test
	public void testUsingFunction() {
		process(new Function<String, Integer>() {
			@Override
			public Integer apply(String s) {
				return s.length();
			}
		}, 10);
	}

	@Test
	public void testStreamWithForEach() {
		Stream.generate(() -> LocalDateTime.now())
			  .limit(100)
			  .distinct()
			  .peek(x -> {
				  System.out.println("In mid stream: " + x);
			  })
			  .filter(localDateTime -> localDateTime.getSecond() % 2 == 0)
			  .forEach(System.out::println);
	}

	@Test
	public void testCollectString() {
		Stream.of("Foo", "Bar", "Baz").collect(Collectors.joining(",")); 
	}

	@Test
	public void testCollect() throws Exception {
		IntStream.range(0, 1000).parallel().map(x -> x + 1).collect(
				new Supplier<List<Integer>>() {
			@Override
			public List<Integer> get() {
				return new ArrayList<>();
			}
		}, new ObjIntConsumer<List<Integer>>() {
			@Override
			public void accept(List<Integer> integers, int value) {
				integers.add(value);
			}
		}, new BiConsumer<List<Integer>, List<Integer>>() {
			@Override
			public void accept(List<Integer> integers, List<Integer> integers2) {
				System.out.println("integers: " + integers + "; integers 2" + integers2);
				integers.addAll(integers2);
			}
		});
	}


	@Test
	public void testMap() {
		List<Double> doubles = Stream.of(1, 2, 3, 4, 5, 6)
									 .map(new Function<Integer, Double>() {
										 @Override
										 public Double apply(Integer integer) {
											 return Double.valueOf(integer) + 3.14;
										 }
									 })
									 .collect(Collectors.toList());
		System.out.println(doubles);
	}

	@Test
	public void testStreamWithCollect() {
		List<Integer> collection = Stream.generate(LocalDateTime::now)
									  .limit(100)
									  .distinct()
									  .filter(localDateTime -> localDateTime.getSecond() % 2 == 0)
									  .map(LocalDateTime::getMinute)
									  .collect(Collectors.toList());
		System.out.println(collection);
	}
}
