package com.lcf;

import com.lcf.base.TimeUtil;
import com.lcf.common.RpcContext;
import com.lcf.common.RpcRequest;
import com.lcf.constants.RpcConstans;
import com.lcf.context.AbstractContext;
import com.lcf.metric.Metric;
import com.lcf.metric.PaladinMetric;

import java.util.concurrent.RejectedExecutionException;


public class MetricContext extends AbstractContext {


    public MetricContext(){
        this(RpcConstans.DEFAULT_SAMPLE_COUNT,RpcConstans.DEFAULT_INTERVAL_IN_MS);
    }

    public MetricContext(int sampleCount,int intervalInMs){
        paladinMetric=new PaladinMetric(sampleCount,intervalInMs);
    }

    private final PaladinMetric paladinMetric;


    @Override
    protected void handle(Object obj) {
        if(obj instanceof RpcRequest){
            RpcContext rpcContext=RpcContext.getContext();
            rpcContext.setStartTime(TimeUtil.currentTimeMillis());
            paladinMetric.addPass(1);
        }
    }

    @Override
    protected void response(Object obj) {
            RpcContext rpcContext=RpcContext.getContext();
            Long startTime=rpcContext.getStartTime();
            if(startTime!=null){
                Long rt=TimeUtil.currentTimeMillis()-startTime;
                paladinMetric.addRT(rt);
                paladinMetric.addSuccess(1);
                logger.warn(rpcContext.getRpcRequest().getClassName()
                        +":"
                        +rpcContext.getRpcRequest().getMethodName()
                        +" 's RT is "
                        +rt);
            }else{
                logger.error(rpcContext.getRpcRequest().getClassName()
                        +":"
                        +rpcContext.getRpcRequest().getMethodName()
                        +"has no start time!");
            }
    }

    @Override
    protected void caughtException(Object obj) {
        paladinMetric.addException(1);
        if(obj instanceof RejectedExecutionException){
            paladinMetric.addReject(1);
        }
    }

    public Metric getPaladinMetric(){
        return paladinMetric;
    }
}
