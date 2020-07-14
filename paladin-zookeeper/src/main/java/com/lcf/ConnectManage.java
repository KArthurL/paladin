package com.lcf;

import com.lcf.netty.handler.ClientInitializer;
import com.lcf.netty.handler.PaladinClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
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

    private CopyOnWriteArrayList<PaladinClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, PaladinClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private long connectTimeoutMillis = 6000;
    private AtomicInteger roundRobin = new AtomicInteger(0);
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

    public void updateConnectedServer(List<String> allServerAddress) {
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
                        connectServerNode(serverNodeAddress);
                    }
                }

                // Close and remove invalid server nodes
                for (int i = 0; i < connectedHandlers.size(); ++i) {
                    PaladinClientHandler connectedServerHandler = connectedHandlers.get(i);
                    SocketAddress remotePeer = connectedServerHandler.getAddress();
                    if (!newAllServerNodeSet.contains(remotePeer)) {
                        logger.info("移除无效节点 " + remotePeer);
                        PaladinClientHandler handler = connectedServerNodes.get(remotePeer);
                        if (handler != null) {
                            handler.close();
                        }
                        connectedServerNodes.remove(remotePeer);
                        connectedHandlers.remove(connectedServerHandler);
                    }
                }

            } else { // No available server node ( All server nodes are down )
                logger.error("No available server node. All server nodes are down !!!");
                for (final PaladinClientHandler connectedServerHandler : connectedHandlers) {
                    SocketAddress remotePeer = connectedServerHandler.getAddress();
                    PaladinClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(connectedServerHandler);
                }
                connectedHandlers.clear();
            }
        }
    }

    public void reconnect(final PaladinClientHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getAddress());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer());

                ChannelFuture channelFuture = b.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server. remote peer = " + remotePeer);
                            PaladinClientHandler handler = channelFuture.channel().pipeline().get(PaladinClientHandler.class);
                            addHandler(handler);
                        }
                    }
                });
            }
        });
    }

    private void addHandler(PaladinClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public PaladinClientHandler chooseHandler() {
        int size = connectedHandlers.size();
        while (isRuning && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    size = connectedHandlers.size();
                }
            } catch (InterruptedException e) {
                logger.error("Waiting for available node is interrupted! ", e);
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return connectedHandlers.get(index);
    }

    public void stop() {
        isRuning = false;
        for (int i = 0; i < connectedHandlers.size(); ++i) {
            PaladinClientHandler connectedServerHandler = connectedHandlers.get(i);
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
