package com.lcf.controller;

import com.lcf.HelloPaladin;
import com.lcf.HelloWorld;
import com.lcf.annotation.RpcReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
public class HelloServiceController {

    @RpcReference
    private HelloWorld helloWorld;

    @RpcReference
    private HelloPaladin helloPaladin;

    @RequestMapping(path = "/helloWorld",method = RequestMethod.GET)
    public String helloWorld(String value){
        return helloWorld.hello(value);
    }

    @RequestMapping(path = "/helloPaladin",method = RequestMethod.GET)
    public String helloPaladin(String value){
        return helloPaladin.helloPaladin(value);
    }
}
