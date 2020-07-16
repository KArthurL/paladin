package com.lcf.data;

import com.lcf.constants.RpcConstans;
import com.lcf.metric.MetricEvent;

import java.util.concurrent.atomic.LongAdder;

/**
 * 参考sentinel实现，存放流量数据
 *
 * @author karthur
 * @date 2020.7.8
 */
public class MetricBucket {

    private final LongAdder[] counters;

    private volatile long minRt;
    public MetricBucket() {
        MetricEvent[] events = MetricEvent.values();
        this.counters = new LongAdder[events.length];
        for (MetricEvent event : events) {
            counters[event.ordinal()] = new LongAdder();
        }
        initMinRt();
    }

    public MetricBucket reset(MetricBucket bucket) {
        for (MetricEvent event : MetricEvent.values()) {
            counters[event.ordinal()].reset();
            counters[event.ordinal()].add(bucket.get(event));
        }
        initMinRt();
        return this;
    }
    private void initMinRt() {
        this.minRt = RpcConstans.DEFAULT_STATISTIC_MAX_RT;
    }

    public MetricBucket reset() {
        for (MetricEvent event : MetricEvent.values()) {
            counters[event.ordinal()].reset();
        }
        initMinRt();
        return this;
    }

    public long get(MetricEvent event) {
        return counters[event.ordinal()].sum();
    }

    public MetricBucket add(MetricEvent event, long n) {
        counters[event.ordinal()].add(n);
        return this;
    }

    public long pass() {
        return get(MetricEvent.PASS);
    }



    public long exception() {
        return get(MetricEvent.EXCEPTION);
    }

    public long rt() {
        return get(MetricEvent.RT);
    }

    public long minRt() {
        return minRt;
    }

    public long success() {
        return get(MetricEvent.SUCCESS);
    }

    public void addPass(int n) {
        add(MetricEvent.PASS, n);
    }



    public void addException(int n) {
        add(MetricEvent.EXCEPTION, n);
    }



    public void addSuccess(int n) {
        add(MetricEvent.SUCCESS, n);
    }

    public void addRT(long rt) {
        add(MetricEvent.RT, rt);

        if (rt < minRt) {
            minRt = rt;
        }
    }
    public void addReject(int n){
        add(MetricEvent.REJECT,n);
    }

    public long reject(){
        return get(MetricEvent.REJECT);
    }

    @Override
    public String toString() {
        return "p: " + pass();
    }
}
