package com.lcf.common.thread;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author k.arthur
 * @date 2020.5.11
 * 底层实现改为数组实现的ThreadLocalMap(参考Netty实现)
 */
public final class PaladinThreadLocalMap {

    private Object[] variables;

    private static ThreadLocal<PaladinThreadLocalMap> slowThreadLocalMap = new ThreadLocal<>();

    private static final AtomicInteger NEXT_INDEX=new AtomicInteger();

    public static final Object UNSET=new Object();

    public static PaladinThreadLocalMap getIfSet(){
        Thread thread=Thread.currentThread();
        if(thread instanceof PaladinThread){
            return ((PaladinThread) thread).getThreadLocalMap();
        }
        return slowThreadLocalMap.get();
    }

    public static PaladinThreadLocalMap get(){
        Thread thread=Thread.currentThread();
        if(thread instanceof PaladinThread){
            return fastGet((PaladinThread) thread);
        }else{
            return slowGet();
        }
    }

    public static void remove(){
        Thread thread=Thread.currentThread();
        if(thread instanceof PaladinThread){
            ((PaladinThread) thread).setThreadLocalMap(null);
        }else{
            slowThreadLocalMap.remove();
        }
    }

    public static void destroy() {
        slowThreadLocalMap = null;
    }
    public static int nextVariableIndex() {
        int index = NEXT_INDEX.getAndIncrement();
        if (index < 0) {
            NEXT_INDEX.decrementAndGet();
            throw new IllegalStateException("Too many thread-local indexed variables");
        }
        return index;
    }

    private PaladinThreadLocalMap() {
        variables = newIndexedVariableTable();
    }
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[32];
        Arrays.fill(array, UNSET);
        return array;
    }

    public Object getVariable(int index){
        return index<variables.length? variables[index]:UNSET;
    }

    public boolean setVariable(int index,Object value){
        if(index<variables.length){
            Object oldValue=variables[index];
            variables[index]=value;
            return oldValue==UNSET;
        }else{
            expandIndexedVariableTableAndSet(index,value);
            return true;
        }
    }
    public Object removeVariable(int index){
        if(index<variables.length){
            Object oldValue=variables[index];
            variables[index]=UNSET;
            return oldValue;
        }else{
            return UNSET;
        }
    }

    public int size(){
        int count=0;
        for(Object o:variables){
            if(o!=UNSET){
                count++;
            }
        }
        //数组首位存放的一个Set，其中记录一些需要清理的ThreadLocal
        return count-1;
    }

    private static PaladinThreadLocalMap fastGet(PaladinThread thread){
        PaladinThreadLocalMap threadLocalMap=thread.getThreadLocalMap();
        if(threadLocalMap==null){
            thread.setThreadLocalMap(threadLocalMap=new PaladinThreadLocalMap());
        }
        return threadLocalMap;
    }

    private static PaladinThreadLocalMap slowGet(){
        ThreadLocal<PaladinThreadLocalMap> slowThreadLocalMap=PaladinThreadLocalMap.slowThreadLocalMap;
        PaladinThreadLocalMap threadLocalMap=slowThreadLocalMap.get();
        if(threadLocalMap==null){
            slowThreadLocalMap.set(threadLocalMap=new PaladinThreadLocalMap());
        }
        return threadLocalMap;
    }

    private void expandIndexedVariableTableAndSet(int index, Object value) {
        Object[] oldArray = variables;
        final int oldCapacity = oldArray.length;
        int newCapacity = index;
        newCapacity |= newCapacity >>> 1;
        newCapacity |= newCapacity >>> 2;
        newCapacity |= newCapacity >>> 4;
        newCapacity |= newCapacity >>> 8;
        newCapacity |= newCapacity >>> 16;
        newCapacity++;

        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        newArray[index] = value;
        variables = newArray;
    }
}
