package com.lcf.loop;


/**
 * @author k.arthur
 * @date 2020.5.12
 *
 * 添加任务失败后执行的策略
 */
public interface RejectedHandler {
    void rejected(Runnable task,PaladinLoop paladinLoop);
}
