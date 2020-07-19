package com.lcf.impl;

import com.lcf.HelloWorld;
import com.lcf.annotation.RpcService;
import com.lcf.base.TimeUtil;
import org.springframework.context.annotation.ComponentScan;

@RpcService
public class HelloWorldImpl implements HelloWorld {

    @Override
    public String hello(String s) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello world "+s;
    }
}
