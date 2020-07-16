package com.lcf.loop;

import com.lcf.constants.RpcConstans;
import com.lcf.threadlocal.PaladinThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;


/**
 * @author k.arthur
 * @date 2020.5.12
 */
public class PaladinLoop extends AbstractExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(PaladinLoop.class);

    private static final int ST_NOT_STARTED = 1;
    private static final int ST_STARTED = 2;
    private static final int ST_SHUTTING_DOWN = 3;
    private static final int ST_SHUTDOWN = 4;
    private static final int ST_TERMINATED = 5;
    private volatile Thread thread;
    private volatile int state=ST_NOT_STARTED;



    private volatile int properties= RpcConstans.MUTI;

    private final int id;

    private PaladinLoopGroup parent;

    private final Queue<Runnable> queue;

    private final Executor executor;
    private final RejectedHandler rejectedHandler;

    private volatile boolean interrupted;

    private static final AtomicIntegerFieldUpdater<PaladinLoop> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(PaladinLoop.class, "state");

    PaladinLoop(PaladinLoopGroup parent,Executor executor,Queue<Runnable> queue,RejectedHandler rejectedHandler,int id){
        this.parent=parent;
        this.executor=executor;
        this.queue=queue;
        this.rejectedHandler=rejectedHandler;
        this.id=id;
    }

    private void run(){
        //to do
    }

    @Override
    public void shutdown() {
        if(isShutdown()){
            return;
        }
        //to do
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return null;
    }

    @Override
    public boolean isShutdown() {
        return state >= ST_SHUTDOWN;
    }


    @Override
    public boolean isTerminated() {
        return state == ST_TERMINATED;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        if(command==null){
            throw new NullPointerException("NO task");
        }
        boolean inEventLoop=inEventLoop();
        addTask(command);
        if(!inEventLoop){
            startThread();
        }
    }

    private void startThread(){
        if(state==ST_NOT_STARTED){
            if(STATE_UPDATER.compareAndSet(this,ST_NOT_STARTED,ST_STARTED)){
                boolean success=false;
                try{
                    doStartThread();
                    success=true;
                }finally {
                    if(!success){
                        STATE_UPDATER.compareAndSet(this,ST_STARTED,ST_NOT_STARTED);
                    }
                }
            }
        }
    }


    private void doStartThread(){
        if(thread!=null){
            throw new IllegalStateException("loop error");
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                thread=Thread.currentThread();
                if(interrupted){
                    thread.interrupt();
                }
                try{
                    PaladinLoop.this.run();
                }catch(Throwable t){
                    logger.error("unexcept error:" +t);
                }finally {
                    for (;;) {
                        int oldState = state;
                        if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER.compareAndSet(
                                PaladinLoop.this, oldState, ST_SHUTTING_DOWN)) {
                            break;
                        }
                    }
                    PaladinThreadLocal.removeAll();
                    STATE_UPDATER.set(PaladinLoop.this, ST_TERMINATED);
                }
            }
        });
    }

    final boolean offerTask(Runnable task) {
        if (isShutdown()) {
            throw new RejectedExecutionException("event executor terminated");
        }
        boolean success =queue.offer(task);
        if(success&&thread!=null){
            LockSupport.unpark(thread);
        }
        return success;
    }

    private void addTask(Runnable task){
        if(task==null){
            throw new NullPointerException("NO task!");
        }
        if(!offerTask(task)){
            reject(task);
        }
    }

    private void reject(Runnable runnable){
        rejectedHandler.rejected(runnable,this);
    }

    public boolean inEventLoop(){
        return Thread.currentThread()==this.thread;
    }
    public void interruptThread(){
        if(thread==null){
            interrupted=true;
        }else{
            thread.interrupt();
        }
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public int getId(){
        return id;
    }
}
