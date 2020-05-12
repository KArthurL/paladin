package com.lcf.loop;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author k.arthur
 * @date 2020.5.12
 *
 */
public class RejectedHandlerFactory {
    public static final int REJECT=1;
    public static final int BACK_OFF=2;

    private RejectedHandlerFactory(){}

    public static RejectedHandler reject(int reject){
        if(reject==REJECT){
            return new RejectedHandler() {
                @Override
                public void rejected(Runnable task, PaladinLoop paladinLoop) {
                    throw new RejectedExecutionException();
                }
            };
        }else if(reject==BACK_OFF){
            RejectedHandler rejectedHandler= reject(BACK_OFF,3,2,TimeUnit.MICROSECONDS);
            return rejectedHandler;
        }else{
            throw new NullPointerException("Wrong parament in RejectedHandler");
        }
    }
    public static RejectedHandler reject(int reject,int retries,long backoffAmount,TimeUnit timeUnit){
        if(reject!=BACK_OFF){
            throw new NullPointerException("Wrong parament in RejectedHandler");
        }else{
            long backOffNanos=timeUnit.toNanos(backoffAmount);
            return new RejectedHandler() {
                @Override
                public void rejected(Runnable task, PaladinLoop paladinLoop) {
                    if(!paladinLoop.inEventLoop()){
                        for(int i=0;i<retries;i++){
                            LockSupport.park(backOffNanos);
                            if(paladinLoop.offerTask(task)){
                                return;
                            }
                        }
                    }

                    throw new RejectedExecutionException();
                }
            };

        }
    }

}
