package com.lcf;

import com.lcf.channel.PaladinChannel;
import com.lcf.channel.PaladinChannelManager;
import com.lcf.constants.RpcConstans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

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

    public void init(int nThreads){
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


    @Override
    public void handle() {

    }

    @Override
    public int getThreadId(String service) {
        return 0;
    }


}
