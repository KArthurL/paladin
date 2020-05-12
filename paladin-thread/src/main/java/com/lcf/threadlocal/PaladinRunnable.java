package com.lcf.threadlocal;



/**
 * @author k.arthur
 * @date 2020.5.11
 * Runnable的包装类，解决ThreadLocal的内存泄漏隐患
 */
public class PaladinRunnable implements Runnable{

    private final Runnable runnable;

    public PaladinRunnable(Runnable runnable){
        this.runnable=runnable;
    }

    @Override
    public void run() {
        try{
            runnable.run();
        }finally {
            PaladinThreadLocal.removeAll();
        }
    }

    public static Runnable wrap(Runnable runnable){
        return runnable instanceof PaladinRunnable?runnable:new PaladinRunnable(runnable);
    }
}
