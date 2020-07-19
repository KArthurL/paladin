package com.lcf.controller;

import com.lcf.HelloPaladin;
import com.lcf.HelloSynWorld;
import com.lcf.HelloWorld;
import com.lcf.annotation.RpcReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RestController
public class HelloServiceController {

    private static final ExecutorService EXECUTOR_SERVICE= Executors.newFixedThreadPool(100);

    @RpcReference
    private HelloWorld helloWorld;

    @RpcReference
    private HelloPaladin helloPaladin;

    @RpcReference
    private HelloSynWorld helloSynWorld;

    @RequestMapping(path = "/helloWorld",method = RequestMethod.GET)
    public String helloWorld(String value){
        return helloWorld.hello(value);
    }

    @RequestMapping(path = "/helloPaladin",method = RequestMethod.GET)
    public String helloPaladin(String value){
        return helloPaladin.helloPaladin(value);
    }

    @RequestMapping(path = "/helloSynn",method = RequestMethod.GET)
    public String helloSyn(String value){
        return helloSynWorld.helloSyn(value);
    }
    @RequestMapping(path = "/start")
    public String startTest(){
        for(int i=0;i<100;i++){
            EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    for(;;){
                        try{
                            helloWorld.hello("test");
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return "suceess";
    }


}
