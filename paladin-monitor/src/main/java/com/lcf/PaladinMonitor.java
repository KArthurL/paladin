package com.lcf;

import com.lcf.channel.PaladinChannel;
import com.lcf.channel.PaladinChannelManager;
import com.lcf.constants.RpcConstans;
import com.lcf.metric.Metric;
import com.lcf.metric.PaladinMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * 监控类,监控服务的流量数据，并进行线程的动态分配
 * @author k.arthur
 * @date 2020.7.15
 */
public class PaladinMonitor implements Monitor {

    private static final Logger LOGGER= LoggerFactory.getLogger(PaladinMonitor.class);

    private final ExecutorService threadPool= Executors.newSingleThreadExecutor();

    private final Map<String,MetricContext> serviceMetrics=new ConcurrentHashMap<>();

    private final PaladinChannelManager paladinChannelManager;



    public PaladinMonitor(PaladinChannelManager paladinChannelManager){
        this.paladinChannelManager=paladinChannelManager;
    }

    public void init(){
        if(paladinChannelManager!=null){
            Set<String> services=paladinChannelManager.getServices();
            int nums=services.size();
            services.forEach(service ->{
                MetricContext metricContext=new MetricContext();
                paladinChannelManager.addContext(service,metricContext);
                serviceMetrics.put(service,metricContext);
            });

            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for(;;){
                        handle();
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            LOGGER.error("moniter has interrupt : %s",e.getCause());
                        }
                    }
                }
            });
        }


    }


    /**
     * 根据策略动态调整线程分配
     */
    @Override
    public void handle() {
        int threads=paladinChannelManager.getThreads();
        int single=paladinChannelManager.getSingleService().size();
        int muti=paladinChannelManager.getMutiService().size();
        int last=((threads-single)/muti)<RpcConstans.MIN_THREADS?(threads-single)/muti:RpcConstans.MIN_THREADS;



    }

    private Map<String,DataModel> getDataMap(){
       Map<String,DataModel> map=new HashMap<>();
       serviceMetrics.entrySet().forEach(entry->{
           String service=entry.getKey();
           Metric paladinMetric=entry.getValue().getPaladinMetric();
           DataModel dataModel=new DataModel();
           dataModel.setException(paladinMetric.exception());
           dataModel.setSuccess(paladinMetric.success());
           dataModel.setPass(paladinMetric.pass());
           dataModel.setReject(paladinMetric.reject());
           dataModel.setRt(paladinMetric.rt());
       });
       return map;
    }


    private class DataModel{
        private long success;
        private long rt;
        private long pass;
        private long exception;
        private long reject;

        public long getSuccess() {
            return success;
        }

        public void setSuccess(long success) {
            this.success = success;
        }

        public long getRt() {
            return rt;
        }

        public void setRt(long rt) {
            this.rt = rt;
        }

        public long getPass() {
            return pass;
        }

        public void setPass(long pass) {
            this.pass = pass;
        }

        public long getException() {
            return exception;
        }

        public void setException(long exception) {
            this.exception = exception;
        }

        public long getReject() {
            return reject;
        }

        public void setReject(long reject) {
            this.reject = reject;
        }

        public void addSuccess(long success){
            this.success+=success;
        }

        public void addRt(long rt) {
            this.rt += rt;
        }

        public void addPass(long pass) {
            this.pass += pass;
        }


        public void addReject(long reject) {
            this.reject+=reject;
        }

        public void addException(long exception) {
            this.exception += exception;
        }
    }


}
