package com.lcf;

/**
 * 根据监控数据进行逻辑处理
 * @author k.arthur
 * @date 2020.7.15
 */
public interface Monitor {


    void handle();
    int getThreadId(String service);

}
