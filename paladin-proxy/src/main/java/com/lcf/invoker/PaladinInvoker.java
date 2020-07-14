package com.lcf.invoker;

import com.lcf.ConnectManage;
import com.lcf.asyn.AsyncInvokeFuture;
import com.lcf.common.RpcRequest;
import com.lcf.constants.RpcConstans;
import com.lcf.netty.common.PaladinMessage;
import com.lcf.netty.handler.PaladinClientHandler;
import com.lcf.protobuffer.ProtobufferSerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * 客户端的代理类
 * @author k.arthur
 * @date 2020.7.13
 */
public class PaladinInvoker implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaladinInvoker.class);


    private final Class<?> clazz;
    private final String service;

    public PaladinInvoker(Class<?> clazz,String service) {
        this.clazz = clazz;
        this.service=service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)  {
        Object o = null;
        //如果是object中的方法就不要走rpc了
        if((o = invokeObjectMethod(proxy, method, args))!=null){
            return o;
        }
        RpcRequest request = buildRequest(method, args);
        PaladinClientHandler paladinClientHandler= ConnectManage.getInstance().chooseHandler(service);
        request.setRemoteAddress(paladinClientHandler.getAddress());
        if(Future.class.isAssignableFrom(method.getReturnType())){
            return paladinClientHandler.Send(request);
        }else{
            AsyncInvokeFuture rpcFuture=paladinClientHandler.Send(request);
            if (!rpcFuture.response.isSuccess()){
                throw new RuntimeException("rpc调用失败，原因："+ rpcFuture.response.getException());
            }
            return rpcFuture.response.getResult();
        }


    }

    private RpcRequest buildRequest(Method method,Object[] args){

        RpcRequest rpcRequest=new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setArguments(args);
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setServiceName(service);
        return rpcRequest;
    }

    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        return null;
    }
}
