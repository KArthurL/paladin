package com.lcf.loop;

import com.lcf.PaladinTask;
import com.lcf.channel.PaladinChannelManager;
import com.lcf.executor.PerTaskExecutor;
import com.lcf.common.thread.PaladinThreadFactory;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


/**
 * @author k.arthur
 * @date 2020.5.12
 *
 */
public class PaladinLoopGroup extends AbstractExecutorService {

    private static final int DEFAULT_MAX_CAPACITANCE=256;
    private static final int DEFAULT_EVENT_LOOP_THREADS=100;
    private static final Logger logger= LoggerFactory.getLogger(PaladinLoopGroup.class);
    private final PaladinLoop[] children;
    private final Executor executor;

    private PaladinChannelManager paladinChannelManager;


    public PaladinLoopGroup(){
        this(DEFAULT_EVENT_LOOP_THREADS);
    }

    public PaladinLoopGroup(int nThreads){
        this(nThreads<=0?DEFAULT_EVENT_LOOP_THREADS:nThreads,
                new PerTaskExecutor(new PaladinThreadFactory(PaladinLoopGroup.class)));
    }

    public PaladinLoopGroup(int nThreads,Executor executor){
        if(nThreads<=0){
            throw new IllegalStateException("nThreads should >0");
        }
        if(executor==null){
            executor=new PerTaskExecutor(new PaladinThreadFactory(PaladinLoopGroup.class));
        }
        this.executor=executor;
        children=new PaladinLoop[nThreads];
        for(int i=0;i<nThreads;i++){
            boolean success=false;
            try{
                children[i]=newChild(i);
                success=true;
            }catch (Exception e){
                logger.error("failed to creat a child"+e);
            }finally {
                if(!success){
                    children[i].shutdown();
                }
            }
        }

        //todo
    }



    public int size(){
        return children.length;
    }

    private PaladinLoop newChild(int id){
        return new PaladinLoop(this
                ,executor
                ,new MpscUnboundedArrayQueue<>(DEFAULT_MAX_CAPACITANCE)
                , RejectedHandlerFactory.reject(RejectedHandlerFactory.REJECT)
                ,id
                );
    }



    @Override
    public void shutdown() {
        for (PaladinLoop l: children) {
            l.shutdown();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        int index= ThreadLocalRandom.current().nextInt(children.length);
        if(command instanceof PaladinTask){
            String service =((PaladinTask) command).getService();
            int n=paladinChannelManager.getIndex(service);
            index=(n>=children.length||n<0)?index:n;
        }
        children[index].execute(command);
    }

    public PaladinChannelManager getPaladinChannelManager() {
        return paladinChannelManager;
    }

    public void setPaladinChannelManager(PaladinChannelManager paladinChannelManager) {
        this.paladinChannelManager = paladinChannelManager;
    }
}
