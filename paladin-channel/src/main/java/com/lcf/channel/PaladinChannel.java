package com.lcf.channel;

import com.lcf.common.RpcContext;

/**
 * 默认的channel
 * @author k.arthur
 * @date 2020.7.14
 */
public class PaladinChannel extends AbstractChannel {

    private PaladinChannelManager paladinChannelManager;

    public PaladinChannel(PaladinChannelManager paladinChannelManager,Class<?> serviceClass, String service, Object invoker) {
        super(serviceClass, service, invoker);
        this.paladinChannelManager=paladinChannelManager;
    }

    @Override
    public void invoke(Object object,io.netty.channel.Channel channel) {
        RpcContext rpcContext=RpcContext.getContext();
        rpcContext.setChannel(channel);
        getPipeline().invoke(object);
    }

    @Override
    public void response(Object object,io.netty.channel.Channel channel) {
        paladinChannelManager.response(object,channel);
    }

    @Override
    public void exception(Object object,io.netty.channel.Channel channel) {
        paladinChannelManager.exception(object,channel);
    }
}
