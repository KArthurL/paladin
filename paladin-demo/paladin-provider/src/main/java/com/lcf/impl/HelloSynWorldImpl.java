package com.lcf.impl;

import com.lcf.HelloSynWorld;
import com.lcf.annotation.RpcService;
import com.lcf.constants.RpcConstans;

@RpcService(type = RpcConstans.SINGLE)
public class HelloSynWorldImpl implements HelloSynWorld {
    @Override
    public String helloSyn(String s) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello Syn world "+s;
    }
}
