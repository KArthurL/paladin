package com.lcf.netty.handler;

import com.lcf.channel.PaladinChannelManager;
import com.lcf.common.RpcRequest;
import com.lcf.common.RpcResponse;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

public class PaladinServerHandler extends SimpleChannelInboundHandler<PaladinMessage>{
    private static final Logger logger = LoggerFactory.getLogger(PaladinServerHandler.class);

    private final ExecutorService executorService;
    private final PaladinChannelManager paladinChannelManager;

    public PaladinServerHandler(ExecutorService executorService,PaladinChannelManager paladinChannelManager){
        this.executorService=executorService;
        this.paladinChannelManager=paladinChannelManager;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PaladinMessage msg) throws Exception {
        if(msg!=null){
            byte[] data=msg.getContent();
            RpcRequest rpcRequest= ProtobufferSerializeUtil.deserializer(data,RpcRequest.class);
            String service=rpcRequest.getServiceName();
            executorService.execute(() ->
                    paladinChannelManager.invoke(rpcRequest,service,ctx.channel()));
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
