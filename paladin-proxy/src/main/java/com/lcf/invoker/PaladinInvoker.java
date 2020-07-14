package com.lcf.invoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 客户端的代理类
 * @author k.arthur
 * @date 2020.7.13
 */
public class PaladinInvoker implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaladinInvoker.class);


    private Class<?> clazz;

    public PaladinInvoker(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
