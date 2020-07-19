package com.lcf.impl;

import com.lcf.HelloPaladin;
import com.lcf.annotation.RpcService;
import com.lcf.constants.RpcConstans;

@RpcService
public class HelloPaladinImpl implements HelloPaladin {

    @Override
    public String helloPaladin(String s) {
        throw new RuntimeException("lalala");
/*     *//*   try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*
        return "hello paladin "+s;*/
    }
}
