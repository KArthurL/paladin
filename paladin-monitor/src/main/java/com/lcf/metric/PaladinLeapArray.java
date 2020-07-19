package com.lcf.metric;

import com.lcf.base.LeapArray;
import com.lcf.base.WindowWrap;
import com.lcf.data.MetricBucket;

public class PaladinLeapArray extends LeapArray<MetricBucket> {

    public PaladinLeapArray(int sampleCount, int intervalInMs) {
         super(sampleCount, intervalInMs);
    }

    @Override
    public MetricBucket newEmptyBucket(long timeMillis) {
        MetricBucket metricBucket=new MetricBucket();
        return metricBucket;
    }

    @Override
    protected WindowWrap<MetricBucket> resetWindowTo(WindowWrap<MetricBucket> windowWrap, long startTime) {
        windowWrap.resetTo(startTime);
        windowWrap.value().reset();
        return windowWrap;
    }
}
