package com.lcf.threadfactory;

import com.lcf.threadlocal.PaladinThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author k.arthur
 * @date 2020.5.11
 */
public class PaladinThreadFactory implements ThreadFactory {

    private final String prefix;
    private final ThreadGroup threadGroup;
    private final AtomicInteger nextId=new AtomicInteger();
    private static final AtomicInteger poolId = new AtomicInteger();

    public static String toPoolName(Class<?> poolType){
        if(poolType==null){
            throw new NullPointerException("poolType");
        }
        String className=poolType.getName().toLowerCase();
        return className;
    }
    public PaladinThreadFactory(Class<?> poolType){
        this(toPoolName(poolType));
    }

    public PaladinThreadFactory(String poolName){
        this(poolName,System.getSecurityManager()==null?
                Thread.currentThread().getThreadGroup():System.getSecurityManager().getThreadGroup());
    }

    public PaladinThreadFactory(String poolName,ThreadGroup threadGroup){
        if(poolName==null){
            throw new NullPointerException("poolName");
        }
        prefix=poolName+'-'+poolId.incrementAndGet()+'-';
        this.threadGroup=threadGroup;
    }



    @Override
    public Thread newThread(Runnable r) {
        Thread t=new PaladinThread(threadGroup,r,prefix+nextId.incrementAndGet());
        t.setDaemon(false);
        return t;
    }
}
