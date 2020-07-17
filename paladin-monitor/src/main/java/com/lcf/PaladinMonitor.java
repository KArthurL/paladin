package com.lcf;

import com.lcf.channel.PaladinChannel;
import com.lcf.channel.PaladinChannelManager;
import com.lcf.constants.RpcConstans;
import com.lcf.metric.Metric;
import com.lcf.metric.PaladinMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

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

    public void init(int ms){
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
                            TimeUnit.MILLISECONDS.sleep(20000);
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
        int last=threads-single;
        int least=(last/muti)<RpcConstans.MIN_THREADS?last/muti:RpcConstans.MIN_THREADS;
        Map<String,DataModel> datas=getDataMap();
        DataModel totalData=mergeData(datas);
        Map<String, List<Integer>> indexs=new HashMap<>();
        List<Map.Entry> list=datas.entrySet().stream()
                .sorted((o1, o2) -> (int)(o2.getValue().getScore()-o1.getValue().getScore()))
                .collect(Collectors.toList());
        for(Map.Entry<String,DataModel> entry:list){
            String service=entry.getKey();
            DataModel dataModel=entry.getValue();
            int t=(int)(last*dataModel.getScore()/totalData.getScore());
            t=t<least?least:t;
            List<Integer> index=new ArrayList<>();
            for(int i=0;i<t;i++){
                if(single==threads){
                    break;
                }
                index.add(single++);
            }
            indexs.put(service,index);
        }
        if(single<threads){
            Map.Entry entry=list.get(list.size()-1);
            List<Integer> index=indexs.get(entry.getKey());
            while(single<threads){
                index.add(single++);
            }

        }
        paladinChannelManager.setIndexs(indexs);


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
           dataModel.setScore(dataModel.getReject()*2
                   +dataModel.getPass()
                   +dataModel.getRt()
                   +dataModel.getException());

       });
       return map;
    }

    private DataModel mergeData(Map<String,DataModel> datas){
        DataModel dataModel=new DataModel();
        if(datas!=null) {
            datas.values().forEach(data -> {
                dataModel.addException(data.getException());
                dataModel.addPass(data.getPass());
                dataModel.addReject(data.getReject());
                dataModel.addRt(data.getRt());
                dataModel.addSuccess(data.getSuccess());
                dataModel.addScore(data.getScore());
            });
        }
        return dataModel;
    }


    private class DataModel{
        private long success;
        private long rt;
        private long pass;
        private long exception;
        private long reject;
        private long score;

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

        public long getScore() {
            return score;
        }

        public void setScore(long score) {
            this.score = score;
        }
        public void addScore(long score){
            this.score=score;
        }
    }


}
