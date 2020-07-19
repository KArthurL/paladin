package com.lcf;

import com.lcf.netty.decoder.PaladinDecoder;
import com.lcf.netty.encoder.PaladinEncoder;
import com.lcf.netty.handler.PaladinClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理zookeeper连接
 */
public class ConnectManage {
    private static final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
    private volatile static ConnectManage connectManage;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(65536));

    //private CopyOnWriteArrayList<PaladinClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, PaladinClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private Map<String,List<PaladinClientHandler>> connectedHandlers=new ConcurrentHashMap<>();


    private ReentrantLock lock = new ReentrantLock();
    private Map<String,Condition> conditions=new ConcurrentHashMap<>();
    private Condition connected = lock.newCondition();
    private long connectTimeoutMillis = 6000;
    private Map<String,AtomicInteger> roundRobins=new ConcurrentHashMap<>();
    //private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRuning = true;

    private ConnectManage() {
    }

    public static ConnectManage getInstance() {
        if (connectManage == null) {
            synchronized (ConnectManage.class) {
                if (connectManage == null) {
                    connectManage = new ConnectManage();
                }
            }
        }
        return connectManage;
    }

    public void updateConnectedServer(String service,List<String> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {  // Get available server node
                //update local serverNodes cache
                HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<InetSocketAddress>();
                for (int i = 0; i < allServerAddress.size(); ++i) {
                    String[] array = allServerAddress.get(i).split(":");
                    if (array.length == 2) { // Should check IP and port
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        final InetSocketAddress remotePeer = new InetSocketAddress(host, port);
                        newAllServerNodeSet.add(remotePeer);
                    }
                }

                // Add new server node
                for (final InetSocketAddress serverNodeAddress : newAllServerNodeSet) {
                    if (!connectedServerNodes.keySet().contains(serverNodeAddress)) {
                        connectServerNode(service,serverNodeAddress);
                    }
                }

                // Close and remove invalid server nodes
                List<PaladinClientHandler> serviceHandlers=connectedHandlers.get(service);
                if(serviceHandlers!=null) {
                    for (int i = 0; i < serviceHandlers.size(); ++i) {
                        PaladinClientHandler connectedServerHandler = serviceHandlers.get(i);
                        SocketAddress remotePeer = connectedServerHandler.getAddress();
                        if (!newAllServerNodeSet.contains(remotePeer)) {
                            logger.info("移除无效节点 " + remotePeer);
                            PaladinClientHandler handler = connectedServerNodes.get(remotePeer);
                            if (handler != null) {
                                handler.close();
                            }
                            connectedServerNodes.remove(remotePeer);
                            serviceHandlers.remove(connectedServerHandler);
                        }
                    }
                }


            } else { // No available server node ( All server nodes are down )
                logger.error("No available server node. All server nodes are down !!!");
                if(connectedHandlers.get(service)!=null) {
                    for (final PaladinClientHandler connectedServerHandler : connectedHandlers.get(service)) {
                        SocketAddress remotePeer = connectedServerHandler.getAddress();
                        PaladinClientHandler handler = connectedServerNodes.get(remotePeer);
                        if(handler!=null) {
                            handler.close();
                        }
                        connectedServerNodes.remove(remotePeer);
                    }
                connectedHandlers.get(service).clear();
                }
            }
        }
    }


    private void connectServerNode(String service, final InetSocketAddress remotePeer) {
        if(!connectedServerNodes.containsKey(remotePeer)) {
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    Bootstrap b = new Bootstrap();
                    b.group(eventLoopGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel.pipeline()
                                            .addLast(new PaladinEncoder())
                                            .addLast(new PaladinDecoder())
                                            .addLast(new PaladinClientHandler(service));
                                }
                            });

                    ChannelFuture channelFuture = b.connect(remotePeer);
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                logger.info("Successfully connect to remote server. remote peer = " + remotePeer);
                                PaladinClientHandler handler = channelFuture.channel().pipeline().get(PaladinClientHandler.class);
                                addHandler(service, handler);
                            }
                        }
                    });
                }
            });
        }else{
            connectedHandlers.getOrDefault(service,new CopyOnWriteArrayList<>()).add(connectedServerNodes.get(remotePeer));
        }
    }

    private void addHandler(String service,PaladinClientHandler handler) {
        if(connectedHandlers.containsKey(service)){
            connectedHandlers.get(service).add(handler);
        }else{
            List<PaladinClientHandler> handlers=new CopyOnWriteArrayList<>();
            handlers.add(handler);
            connectedHandlers.put(service,handlers);
        }
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler(service);
    }

    private void signalAvailableHandler(String service) {
        lock.lock();
        try {
            conditions.getOrDefault(service,lock.newCondition()).signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler(String service) throws InterruptedException {
        lock.lock();
        try {
            return conditions.getOrDefault(service,lock.newCondition()).await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public PaladinClientHandler chooseHandler(String service) {
        List<PaladinClientHandler> handlers=connectedHandlers.get(service);
        int size = handlers==null?-1:handlers.size();
        while (isRuning && size <= 0) {
            try {
                boolean available = waitingForHandler(service);
                if (available) {
                    size = handlers.size();
                }
            } catch (InterruptedException e) {
                logger.error("Waiting for available node is interrupted! ", e);
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        AtomicInteger roundRobin=roundRobins.getOrDefault(service,new AtomicInteger(0));
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return handlers.get(index);
    }


}
