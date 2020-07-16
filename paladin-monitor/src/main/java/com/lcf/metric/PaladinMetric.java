package com.lcf.metric;

import com.lcf.base.LeapArray;
import com.lcf.base.WindowWrap;
import com.lcf.constants.RpcConstans;
import com.lcf.data.MetricBucket;

import java.util.List;

/**
 * 流量数据统计的实现类
 * @author karthur
 * @date 2020.7.9
 */
public class PaladinMetric implements Metric{

    private final LeapArray<MetricBucket> data;

    public PaladinMetric(int sampleCount,int intervalInMs){
        this.data=new PaladinLeapArray(sampleCount,intervalInMs);
    }


    @Override
    public long success() {
        data.currentWindow();
        long success = 0;

        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            success += window.success();
        }
        return success;
    }

    @Override
    public long maxSuccess() {
        data.currentWindow();
        long success = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.success() > success) {
                success = window.success();
            }
        }
        return Math.max(success, 1);
    }

    @Override
    public long exception() {
        data.currentWindow();
        long exception = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            exception += window.exception();
        }
        return exception;
    }

    @Override
    public long pass() {
        data.currentWindow();
        long pass = 0;
        List<MetricBucket> list = data.values();

        for (MetricBucket window : list) {
            pass += window.pass();
        }
        return pass;
    }

    @Override
    public long rt() {
        data.currentWindow();
        long rt = 0;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            rt += window.rt();
        }
        return rt;
    }

    @Override
    public long minRt() {
        data.currentWindow();
        long rt = RpcConstans.DEFAULT_STATISTIC_MAX_RT;
        List<MetricBucket> list = data.values();
        for (MetricBucket window : list) {
            if (window.minRt() < rt) {
                rt = window.minRt();
            }
        }

        return Math.max(1, rt);
    }

    @Override
    public MetricBucket[] windows() {
        data.currentWindow();
        return data.values().toArray(new MetricBucket[0]);
    }

    @Override
    public void addException(int n) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addException(n);
    }

    @Override
    public void addSuccess(int n) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addSuccess(n);
    }

    @Override
    public void addPass(int n) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addPass(n);
    }

    @Override
    public void addRT(long rt) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addRT(rt);
    }

    @Override
    public void addReject(int n) {
        WindowWrap<MetricBucket> wrap = data.currentWindow();
        wrap.value().addReject(n);
    }

    @Override
    public long reject() {
        data.currentWindow();
        long pass = 0;
        List<MetricBucket> list = data.values();

        for (MetricBucket window : list) {
            pass += window.reject();
        }
        return pass;
    }

    @Override
    public double getWindowIntervalInSec() {
       return data.getIntervalInSecond();
    }

    @Override
    public int getSampleCount() {
        return data.getSampleCount();
    }

    @Override
    public long getWindowPass(long timeMillis) {
        MetricBucket bucket = data.getWindowValue(timeMillis);
        if (bucket == null) {
            return 0L;
        }
        return bucket.pass();
    }
}
