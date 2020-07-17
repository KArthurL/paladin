package com.lcf.common.thread;


/**
 * @author k.arthur
 * @date 2020.5.11
 */
public class PaladinThread extends Thread {

    private PaladinThreadLocalMap threadLocalMap;

    public PaladinThread(){
    }

    public PaladinThread(Runnable runnable){
        super(PaladinRunnable.wrap(runnable));
    }
    public PaladinThread(ThreadGroup group,Runnable runnable,String name){
        super(group,PaladinRunnable.wrap(runnable),name);
    }

    public PaladinThreadLocalMap getThreadLocalMap() {
        return threadLocalMap;
    }

    public void setThreadLocalMap(PaladinThreadLocalMap threadLocalMap) {
        this.threadLocalMap = threadLocalMap;
    }
}
