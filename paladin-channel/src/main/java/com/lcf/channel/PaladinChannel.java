package com.lcf.channel;

import com.lcf.common.RpcContext;
import com.lcf.common.RpcRequest;
import io.netty.channel.Channel;

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

    @Override
    public void reject(Object request,Object object, Channel channel) {
        RpcContext rpcContext=RpcContext.getContext();
        rpcContext.setChannel(channel);
        if (request instanceof RpcRequest) {
            RpcRequest rpcRequest = (RpcRequest) request;
            rpcContext.setRpcRequest(rpcRequest);
        }
        getPipeline().exception(object);
    }
}
