package com.lcf.boot;

import com.lcf.Entry;
import com.lcf.PaladinMonitor;
import com.lcf.PaladinServerHandler;
import com.lcf.ServiceRegistry;
import com.lcf.annotation.RpcService;
import com.lcf.channel.PaladinChannelManager;
import com.lcf.constants.RpcConstans;
import com.lcf.loop.PaladinLoopGroup;
import com.lcf.netty.decoder.PaladinDecoder;
import com.lcf.netty.encoder.PaladinEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 服务端的启动类
 * @author k.arthur
 * @date 2020.7.17
 */
@Component
public class ServerBoot implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ServerBoot.class);
    @Value("${rpc.threadpool.threads}")
    private int threads;

    @Value("${rpc.service.address}")
    private String serverAddress;

    @Value("${rpc.threads.ditribution.time}")
    private int threadsDistribution;

    @Autowired
    private ServiceRegistry serviceRegistry;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    void init(ExecutorService executorService,PaladinChannelManager paladinChannelManager) throws InterruptedException {
        if (bossGroup == null && workerGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new PaladinDecoder())
                                    .addLast(new PaladinEncoder())
                                    .addLast(new PaladinServerHandler(executorService,paladinChannelManager));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("服务启动在端口 {}", port);
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        Map<String, Entry> services=new HashMap<>();
        if (beansWithAnnotation!=null){
            for (Object value : beansWithAnnotation.values()) {
                String clazz = value.getClass().getInterfaces()[0].getName();
                RpcService rpcService = value.getClass().getAnnotation(RpcService.class);
                String group = rpcService.group();
                String version = rpcService.version();
                String service = clazz + ":" + version + "_" + group;
                int type=rpcService.type();

                Entry entry=new Entry();
                entry.setClazz(value.getClass());
                entry.setType(type);
                entry.setInvoker(value);
                services.put(service,entry);
            }
        }
        if(threads==0){
            threads= RpcConstans.DEFAULT_THREADS;
        }
        PaladinLoopGroup paladinLoopGroup=new PaladinLoopGroup(threads);
        PaladinChannelManager paladinChannelManager=new PaladinChannelManager(services);
        paladinChannelManager.init(threads);
        paladinLoopGroup.setPaladinChannelManager(paladinChannelManager);
        PaladinMonitor paladinMonitor=new PaladinMonitor(paladinChannelManager);
        paladinMonitor.init(threadsDistribution);
        try {
            init(paladinLoopGroup,paladinChannelManager);

        } catch (InterruptedException e) {
            logger.error("Server start fail, error: {}",e.getCause());
        }
        if(serviceRegistry==null || serverAddress==null){
            logger.error("serviceRegistry is null");
        }else{
            services.keySet().forEach(service->{
                logger.info("正在注册服务: {}", service);
                serviceRegistry.register(service,serverAddress);
            });
        }

    }
}
