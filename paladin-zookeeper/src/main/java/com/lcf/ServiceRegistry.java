package com.lcf;

import com.lcf.constants.RpcConstans;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册
 *
 */
@Component
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);
    @Value("${rpc.registry.address}")
    private String registryAddress;

    private ZooKeeper zookeeper;


    public void register(String service,String ip) {
        if (service != null && ip !=null) {
            if(zookeeper==null){
                zookeeper=connectServer();
            }
                AddRootNode(zookeeper,service); // Add root node if not exist
                createNode(zookeeper, service, ip);

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
        } catch (IOException e) {
            logger.error("connect to ZK has failed", e);
        }
        catch (InterruptedException ex){
            logger.error("connect to ZK has been interrupted", ex);
        }
        return zk;
    }

    private void AddRootNode(ZooKeeper zk,String service){
        try {
            Stat s = zk.exists(RpcConstans.ZK_REGISTRY_PATH+"/"+service, false);
            if (s == null) {
                zk.create(RpcConstans.ZK_REGISTRY_PATH+"/"+service, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            logger.error(e.toString());
        } catch (InterruptedException e) {
            logger.error(e.toString());
        }
    }

    private void createNode(ZooKeeper zk, String service,String ip) {
        try {
            byte[] ipBytes=ip.getBytes();
            String path = zk.create(RpcConstans.ZK_REGISTRY_PATH+"/"+service+"/"+"data", ipBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("已创建zookeeper节点 ({} => {})", path, ip);
        } catch (KeeperException e) {
            logger.error("", e);
        }
        catch (InterruptedException ex){
            logger.error("", ex);
        }
    }
}