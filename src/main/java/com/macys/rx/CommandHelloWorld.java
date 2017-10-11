package com.macys.rx;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class CommandHelloWorld extends HystrixCommand<String> {
    private final String name;

    public CommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("myGroupName"));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        System.out.println(Thread.currentThread().getName());
//        Thread.sleep(5000);
        return "Hello " + name;
    }
}
