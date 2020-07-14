package com.lcf.netty.handler;

import com.lcf.annotation.RpcReference;
import com.lcf.asyn.AsyncInvokeFuture;
import com.lcf.common.RpcRequest;
import com.lcf.common.RpcResponse;
import com.lcf.constants.RpcConstans;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.*;

/**
 * 客户端的handler
 * @author k.arthur
 * @date 2020.7.14
 */
public class PaladinClientHandler extends SimpleChannelInboundHandler<PaladinMessage> {

    private static final Logger logger = LoggerFactory.getLogger(PaladinClientHandler.class);

    private static final ConcurrentHashMap<String, AsyncInvokeFuture> invokeMap = new ConcurrentHashMap<>();

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(65536));


    private volatile Channel channel;
    private SocketAddress address;

    private final String service;

    public PaladinClientHandler(String service){
        this.service=service;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.address = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PaladinMessage msg) throws Exception {
        if(msg!=null) {
            RpcResponse response=ProtobufferSerializeUtil.deserializer(msg.getContent(),RpcResponse.class);
            logger.info("收到来自服务器的响应, rpcResponse:{}", response);

            if(response.isSuccess()) {
                Object result = response.getResult();
                if (result instanceof AsyncInvokeFuture) {
                    response.setResult(((AsyncInvokeFuture) result).response.getResult());
                }
                AsyncInvokeFuture future = invokeMap.remove(response.getRequestId());
                if (future != null) {
                    future.recive(response);
                    runCallBack(future);

                }
            }else{

            }
        }
    }

    private void runCallBack(AsyncInvokeFuture asyncInvokeFuture) {
        if (asyncInvokeFuture.getCallBack() != null) {
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (asyncInvokeFuture.response.getException()==null){
                        asyncInvokeFuture.getCallBack().success(asyncInvokeFuture.response.getResult());
                    }else {
                        asyncInvokeFuture.getCallBack().fail(asyncInvokeFuture.response.getResult());
                    }
                }
            });
        }
    }





    public AsyncInvokeFuture Send(RpcRequest rpcRequest) {
        AsyncInvokeFuture future = new AsyncInvokeFuture();
        byte[] data= ProtobufferSerializeUtil.serializer(rpcRequest);
        PaladinMessage paladinMessage=new PaladinMessage(data.length, RpcConstans.RPC_INVOKE,data);
        invokeMap.put(rpcRequest.getRequestId(),future);
        channel.writeAndFlush(paladinMessage);
        return future;
    }



    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
