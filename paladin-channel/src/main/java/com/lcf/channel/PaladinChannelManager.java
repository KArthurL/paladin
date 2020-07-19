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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

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
    private  final Map<String, LongAdder> roundRobins=new HashMap<>();
    private  final Map<String,Integer> singleIndexs=new HashMap();
    private  volatile Map<String,List<Integer>> indexs=new HashMap<>();

    private int threads=0;
    private int size=0;
    public PaladinChannelManager (Map<String, Entry> map){
        if(map!=null){
            size=map.keySet().size();
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
                roundRobins.put(service,new LongAdder());
            });
        }
    }

    public synchronized void init(int nThreads){
        threads=nThreads;
        int size=singleService.size()+mutiService.size();
        if(size>nThreads){
            throw new RuntimeException("Too many providers, please increase the number of threads! ");
        }else{
            int id=0;
            for(String singleService:singleService){
                singleIndexs.put(singleService,id++);
            }
            int last=nThreads-id;
            int avg=last/mutiService.size();
            for(String service:mutiService){
                List<Integer> list=new ArrayList<>();
                for(int i=0;i<avg;i++){
                    list.add(id++);
                }
                if((last-id)<avg){
                    for(int i=id;i<nThreads;i++){
                        list.add(i++);
                    }
                }
                indexs.put(service,list);
            }

        }
    }



    public void invoke(Object object, String service, io.netty.channel.Channel channel){
        com.lcf.channel.Channel channel1=channels.get(service);
        if(channel1!=null){
            logger.info("request is invoking, service: {}, request: {}",service,object);
            channel1.invoke(object,channel);
        }else{
           logger.error("there is no service for :{}",service);
        }
    }

    public void response(Object object,io.netty.channel.Channel channel){
        PaladinMessage response=buildMessage(object);
        channel.writeAndFlush(response);
        logger.info("request has been response: {}",object.toString());

    }

    public void exception(Object object,io.netty.channel.Channel channel){
        PaladinMessage response=buildMessage(object);
        channel.writeAndFlush(response);
        logger.info("request has exception : {}",object);
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

    public synchronized void setIndexs(Map<String,List<Integer>> indexs){
        this.indexs=indexs;
    }

    public int getIndex(String service){
        if(singleIndexs.containsKey(service)){
            return singleIndexs.get(service)==null?0:singleIndexs.get(service);
        }
        LongAdder longAdder=roundRobins.get(service);
        List<Integer> list=indexs.get(service);
        if(list!=null&&list.size()>0){
            longAdder.increment();
            long x=longAdder.longValue();
            if(x<0){
                //可能会重复设置，但问题不大
                longAdder.reset();
                return list.get(0);
            }else{
                int size=list.size();
                return list.get((int)(x%(Integer.valueOf(size).longValue())));
            }

        }
        return -1;

    }

    public int getThreads() {
        return threads;
    }

    public int getSize() {
        return size;
    }
}
