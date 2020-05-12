package com.lcf.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;


/**
 * @author k.arthur
 * @date 2020.5.12
 */
public class PerTaskExecutor implements Executor {

    private final ThreadFactory threadFactory;
    public PerTaskExecutor(ThreadFactory threadFactory){
        if(threadFactory==null){
            throw new NullPointerException("NO threadFactory!!!");
        }
        this.threadFactory=threadFactory;
    }


    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
    }
}
