package com.lcf;

public abstract class PaladinTask implements Runnable {

    private String service;

    public PaladinTask(String service){
        this.service=service;
    }

    public String getService() {
        return service;
    }
}
