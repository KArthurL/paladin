package com.lcf.common;


import com.lcf.common.thread.PaladinThreadLocal;
import io.netty.channel.Channel;

public class RpcContext {

    private static final PaladinThreadLocal<RpcContext> LOCAL_CONTEXT=new PaladinThreadLocal(){
        @Override
        protected RpcContext initialValue() throws Exception {
            return new RpcContext();
        }
    };

    private String traceId;

    private Long startTime;

    private RpcRequest rpcRequest;

    private RpcResponse rpcResponse;

    private Channel channel;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }


    public RpcRequest getRpcRequest() {
        return rpcRequest;
    }

    public void setRpcRequest(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    public static RpcContext getContext(){
        return LOCAL_CONTEXT.get();
    }
    public static void remove(){
        LOCAL_CONTEXT.remove();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "RpcContext{" +
                "traceId='" + traceId + '\'' +
                ", startTime=" + startTime +
                ", rpcRequest=" + rpcRequest +
                ", rpcResponse=" + rpcResponse +
                ", channel=" + channel +
                '}';
    }
}
