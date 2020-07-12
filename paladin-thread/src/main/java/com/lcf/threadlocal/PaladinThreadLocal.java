package com.lcf.threadlocal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * @author k.arthur
 * @date 2020.5.11
 */
public class PaladinThreadLocal<V> {

    private  static final int VARIABLES_TO_REMOVE_INDEX=PaladinThreadLocalMap.nextVariableIndex();

    private final int index;

    public PaladinThreadLocal(){
        index=PaladinThreadLocalMap.nextVariableIndex();
    }

    public static int size(){
        PaladinThreadLocalMap threadLocalMap=PaladinThreadLocalMap.getIfSet();
        if(threadLocalMap==null){
            return 0;
        }else{
            return threadLocalMap.size();
        }
    }

    public static void destroy(){
        PaladinThreadLocalMap.destroy();
    }

    private static void addToVariablesToRemove(PaladinThreadLocalMap threadLocalMap, PaladinThreadLocal<?> variable) {
        Object v = threadLocalMap.getVariable(VARIABLES_TO_REMOVE_INDEX);
        Set<PaladinThreadLocal<?>> variablesToRemove;
        if (v == PaladinThreadLocalMap.UNSET || v == null) {
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<PaladinThreadLocal<?>, Boolean>());
            threadLocalMap.setVariable(VARIABLES_TO_REMOVE_INDEX, variablesToRemove);
        } else {
            variablesToRemove = (Set<PaladinThreadLocal<?>>) v;
        }

        variablesToRemove.add(variable);
    }

    @SuppressWarnings("unchecked")
    private static void removeFromVariablesToRemove(PaladinThreadLocalMap threadLocalMap, PaladinThreadLocal<?> variable) {

        Object v = threadLocalMap.getVariable(VARIABLES_TO_REMOVE_INDEX);

        if (v == PaladinThreadLocalMap.UNSET || v == null) {
            return;
        }

        Set<PaladinThreadLocal<?>> variablesToRemove = (Set<PaladinThreadLocal<?>>) v;
        variablesToRemove.remove(variable);
    }

    public final V get(){
        PaladinThreadLocalMap threadLocalMap =PaladinThreadLocalMap.get();
        Object v=threadLocalMap.getVariable(index);
        if(v!=PaladinThreadLocalMap.UNSET){
            return (V) v;
        }else{
            return initialize(threadLocalMap);
        }
    }

    private V initialize(PaladinThreadLocalMap threadLocalMap) {
        V v = null;
        try {
            v = initialValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        threadLocalMap.setVariable(index, v);
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }

    public final void set(V value){
        if(value==null||value==PaladinThreadLocalMap.UNSET){
            remove(PaladinThreadLocalMap.getIfSet());
        }else{
            PaladinThreadLocalMap threadLocalMap=PaladinThreadLocalMap.get();
            if(threadLocalMap.setVariable(index,value)){
                addToVariablesToRemove(threadLocalMap,this);
            }
        }
    }
    public final void remove(PaladinThreadLocalMap threadLocalMap){
        if(threadLocalMap==null){
            return;
        }
        Object v=threadLocalMap.removeVariable(index);
        removeFromVariablesToRemove(threadLocalMap,this);
    }

    public final void remove() {
        remove(PaladinThreadLocalMap.getIfSet());
    }

    public static void removeAll(){
        PaladinThreadLocalMap threadLocalMap=PaladinThreadLocalMap.getIfSet();
        if(threadLocalMap==null){
            return;
        }
        Object v=threadLocalMap.getVariable(VARIABLES_TO_REMOVE_INDEX);
        if(v!=null&&v!=PaladinThreadLocalMap.UNSET){
            Set<PaladinThreadLocal<?>> varivlesToRemove=(Set<PaladinThreadLocal<?>>) v;
            for(PaladinThreadLocal<?> tlv:varivlesToRemove){
                tlv.remove(threadLocalMap);
            }
        }
        PaladinThreadLocalMap.remove();
    }

    protected V initialValue() throws Exception {
        return null;
    }

}
