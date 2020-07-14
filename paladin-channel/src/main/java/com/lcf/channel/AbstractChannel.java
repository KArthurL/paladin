package com.lcf.channel;

import com.lcf.context.Context;
import com.lcf.pipeline.DefaultPaladinPipeline;
import com.lcf.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * channel的抽象类，初始化默认的pipeline
 * @author k.arthur
 * @date 2020.7.14
 */
public  abstract class AbstractChannel implements Channel{

    private static final Logger logger= LoggerFactory.getLogger(AbstractChannel.class);
    private static final AtomicInteger nextId=new AtomicInteger(0);

    private final Class<?> serviceClass;
    private final String service;
    private Pipeline pipeline;
    private int id;

    public AbstractChannel(Class<?> serviceClass,String service,Object invoker){
        this.id=nextId.getAndAdd(1);
        this.service=service;
        this.serviceClass=serviceClass;
        pipeline=new DefaultPaladinPipeline(this,serviceClass,invoker);

    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public List<Context> getContexts() {
        return pipeline.getContexts();
    }
}
