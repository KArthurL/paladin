package com.lcf.asyn;

import com.lcf.common.RpcResponse;

import java.util.concurrent.*;

/**
 * @author k.arthur
 * @date 2020.7.14
 */
public class AsyncInvokeFuture implements Future {

    public RpcResponse response;

    public CountDownLatch countDownLatch = new CountDownLatch(1);

    private AsyncCallBack callBack;

    public AsyncCallBack getCallBack() {
        return callBack;
    }

    public void addCallBack(AsyncCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount()==0;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException{
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.getResult();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException{
        try {
            countDownLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.getResult();
    }

    public void recive(RpcResponse rpcResponse){
        this.response=rpcResponse;
        countDownLatch.countDown();
    }
}
