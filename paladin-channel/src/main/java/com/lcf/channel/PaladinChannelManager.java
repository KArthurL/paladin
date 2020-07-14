package com.lcf.channel;

import com.lcf.common.RpcContext;
import com.lcf.common.RpcRequest;
import com.lcf.common.RpcResponse;
import com.lcf.constants.RpcConstans;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaladinChannelManager {


    private static final Logger logger= LoggerFactory.getLogger(PaladinChannelManager.class);
    private static final Map<String, com.lcf.channel.Channel> channels=new ConcurrentHashMap<>();

    public void invoke(Object object, String service, io.netty.channel.Channel channel){
        com.lcf.channel.Channel channel1=channels.get(service);
        if(channel1!=null){
            channel1.invoke(object,channel);
        }else{
           logger.error("there is no service for : %s",service);
        }
    }

    public void response(Object object,io.netty.channel.Channel channel){
        PaladinMessage response=buildMessage(object);
        channel.writeAndFlush(response);

    }

    public void exception(Object object,io.netty.channel.Channel channel){
        PaladinMessage response=buildMessage(object);
        channel.writeAndFlush(response);
    }

    public PaladinMessage buildMessage(Object object){
        byte[] data= ProtobufferSerializeUtil.serializer(object);
        PaladinMessage paladinMessage=new PaladinMessage(data.length,RpcConstans.RPC_INVOKE,data);
        return paladinMessage;
    }



}
