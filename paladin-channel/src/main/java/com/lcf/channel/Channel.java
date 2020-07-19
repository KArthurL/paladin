package com.lcf.channel;

import com.lcf.context.Context;
import com.lcf.pipeline.Pipeline;

import java.util.List;

/**
 * 不同服务所对应的channel
 * @author k.arthur
 * @date 2020.7.12
 */
public interface Channel {

    int id();

    Class<?> getServiceClass();

    Pipeline getPipeline();

    List<Context> getContexts();

    void  invoke(Object object, io.netty.channel.Channel channel);

    void response(Object object, io.netty.channel.Channel channel);

    void exception(Object object, io.netty.channel.Channel channel);

    void reject(Object request,Object object,io.netty.channel.Channel channel);

    Context getContext(Class<?> clazz);

}
