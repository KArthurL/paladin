package com.lcf;

public class Entry {
    private Class<?> clazz;
    private Object invoker;
    private int type;

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getInvoker() {
        return invoker;
    }

    public void setInvoker(Object invoker) {
        this.invoker = invoker;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
