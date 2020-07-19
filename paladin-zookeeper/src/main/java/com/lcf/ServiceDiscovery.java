package com.lcf;

import com.lcf.constants.RpcConstans;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 服务发现
 *
 *
 */
@Component
public class ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private static final  Map<String,List<String>> servicedata= new ConcurrentHashMap<>();

    @Value("${rpc.registry.address}")
    private String registryAddress;
    private ZooKeeper zookeeper;


    @PostConstruct
    private  void init(){
        logger.info("zookeeper 开始初始化");
        if(registryAddress==null){
            registryAddress=RpcConstans.ZK_DEFAULT_ADDRESS;
        }
        zookeeper=connectServer();
    }



    public void discover(String service) {
        if(zookeeper!=null){

            watchNode(zookeeper,service);
        }else{
            logger.error("zookeeper init fail");
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, RpcConstans.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            logger.error("", e);
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk,String service) {
        try {
            List<String> nodeList = zk.getChildren(RpcConstans.ZK_REGISTRY_PATH+"/"+service, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk,service);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(RpcConstans.ZK_REGISTRY_PATH +"/"+service+ "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            logger.info("节点数据: {}", dataList);
            servicedata.put(service,dataList);

            logger.info("Service discovery triggered updating connected server node.");
            UpdateConnectedServer(service,dataList);
        } catch (KeeperException | InterruptedException e) {
            logger.error("", e);
        }
    }

    private void UpdateConnectedServer(String service,List<String> dataList){
        ConnectManage.getInstance().updateConnectedServer(service,dataList);
    }

    public void stop(){
        if(zookeeper!=null){
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
