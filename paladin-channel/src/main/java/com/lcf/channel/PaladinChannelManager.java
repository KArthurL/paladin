package com.lcf.channel;

import com.lcf.Entry;
import com.lcf.MetricContext;
import com.lcf.Monitor;
import com.lcf.common.RpcContext;
import com.lcf.common.RpcRequest;
import com.lcf.common.RpcResponse;
import com.lcf.constants.RpcConstans;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.pipeline.DefaultPaladinPipeline;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class PaladinChannelManager {

    private static final Logger logger= LoggerFactory.getLogger(PaladinChannelManager.class);
    private  final Map<String, com.lcf.channel.Channel> channels=new ConcurrentHashMap<>();

    private final Monitor monitor;
    public PaladinChannelManager (Map<String, Entry> map,Monitor monitor){
        this.monitor=monitor;
        if(map!=null){
            map.entrySet().forEach(elemnt ->{
                String service =elemnt.getKey();
                Entry entry=elemnt.getValue();
                PaladinChannel paladinChannel=new PaladinChannel(this
                        ,entry.getClazz()
                        ,service
                        ,entry.getInvoker());
                MetricContext metricContext=new MetricContext();
                paladinChannel.getPipeline().addContext(metricContext);
                monitor.addMetric(service,metricContext);
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



}
