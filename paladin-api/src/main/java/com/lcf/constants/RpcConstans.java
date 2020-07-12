package com.lcf.constants;

public class RpcConstans {

    public static final  int ZK_SESSION_TIMEOUT = 5000;
    public static final  String ZK_REGISTRY_PATH = "/registry";
    public static final  String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
    public static final int MESSAGE_HEAD=0X76;

    public static final int DEFAULT_STATISTIC_MAX_RT=5000;
    public static final int MIN_THREADS=5;

    /**
     * 默认统计一分钟，划分为60个时间片
     */
    public static final int DEFAULT_SAMPLE_COUNT=60;
    public static final int DEFAULT_INTERVAL_IN_MS=60*1000;


}
