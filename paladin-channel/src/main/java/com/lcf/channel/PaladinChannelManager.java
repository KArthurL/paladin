package com.lcf.channel;

import com.lcf.Entry;
import com.lcf.common.RpcContext;
import com.lcf.common.RpcRequest;
import com.lcf.common.RpcResponse;
import com.lcf.constants.RpcConstans;
import com.lcf.context.Context;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.pipeline.DefaultPaladinPipeline;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理service的类
 *
 * @author k.arthur
 * @date 2020.7.15
 */

public class PaladinChannelManager{

    private static final Logger logger= LoggerFactory.getLogger(PaladinChannelManager.class);
    private  final Map<String, com.lcf.channel.Channel> channels=new ConcurrentHashMap<>();
    private  final Set<String> singleService=new HashSet<>();
    private  final Set<String> mutiService=new HashSet<>();


    public PaladinChannelManager (Map<String, Entry> map){

        if(map!=null){
            map.entrySet().forEach(elemnt ->{
                String service =elemnt.getKey();
                Entry entry=elemnt.getValue();
                PaladinChannel paladinChannel=new PaladinChannel(this
                        ,entry.getClazz()
                        ,service
                        ,entry.getInvoker());
                channels.put(service,paladinChannel);
                if(RpcConstans.SINGLE_TYPE.equals(entry.getType())){
                    singleService.add(service);
                }else{
                    mutiService.add(service);
                }
            });
        }
    }



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

    public void addContext(String service, Context context){
        if(channels.containsKey(service)){
            Channel channel=channels.get(service);
            channel.getPipeline().addContext(context);
        }
    }

    public Set<String> getServices(){
        return channels.keySet();
    }

    public Set<String> getSingleService() {
        return singleService;
    }

    public Set<String> getMutiService() {
        return mutiService;
    }
}
