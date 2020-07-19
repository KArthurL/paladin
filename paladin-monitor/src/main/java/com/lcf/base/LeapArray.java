package com.lcf.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用时间窗口法统计流量的基类，参考sentinel
 * @param <T>
 * @author karthur
 * @date 2020.7.9
 *
 */
public abstract class LeapArray<T> {

    /**
     * 时间片跨度 = 时间窗口的时间跨度/时间片数量
     */
    protected int windowLengthInMs;
    /**
     * 时间窗口中时间片的数量
     */
    protected int sampleCount;
    /**
     * 时间窗口的时间跨度
     */
    protected int intervalInMs;

    protected final AtomicReferenceArray<WindowWrap<T>> array;

    private final ReentrantLock updateLock = new ReentrantLock();
    public LeapArray(int sampleCount, int intervalInMs) {
        if(sampleCount<=0){
            throw new IllegalArgumentException("bucket count is invalid: " + sampleCount);
        }
        if(intervalInMs<=0){
            throw new IllegalArgumentException("total time interval of the sliding window should be positive");
        }
        if(intervalInMs % sampleCount != 0) {
            throw new IllegalArgumentException("time span needs to be evenly divided");
        }

        this.windowLengthInMs = intervalInMs / sampleCount;
        this.intervalInMs = intervalInMs;
        this.sampleCount = sampleCount;

        this.array = new AtomicReferenceArray<>(sampleCount);
    }


    public abstract T newEmptyBucket(long timeMillis);

    public WindowWrap<T> currentWindow() {
        return currentWindow(TimeUtil.currentTimeMillis());
    }
    protected abstract WindowWrap<T> resetWindowTo(WindowWrap<T> windowWrap, long startTime);

    /**
     * 根据当前时间确定时间片的index
     * @param timeMillis 时间（ms）
     * @return
     */
    private int calculateTimeIdx(long timeMillis) {
        long timeId = timeMillis / windowLengthInMs;
        return (int)(timeId % array.length());
    }

    /**
     * 计算该窗口时间的起始值
     * @param timeMillis 时间（ms）
     * @return
     */
    protected long calculateWindowStart(long timeMillis) {
        return timeMillis - timeMillis % windowLengthInMs;
    }
    public WindowWrap<T> currentWindow(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        //计算时间片的index和该时间片的起始时间
        int idx = calculateTimeIdx(timeMillis);
        long windowStart = calculateWindowStart(timeMillis);
        while (true) {
            WindowWrap<T> old = array.get(idx);
            if (old == null) {
                //说明该时间片还没有初始化
                WindowWrap<T> window = new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket(timeMillis));
                //cas设置该位置的时间片
                if (array.compareAndSet(idx, null, window)) {
                    return window;
                } else {
                    //cas失败说明有竞争，让出cpu
                    Thread.yield();
                }
            } else if (windowStart == old.windowStart()) {
                //说明该时间片还在有效期
                return old;
            } else if (windowStart > old.windowStart()) {
                //说明该时间过期了，需要更新
                if (updateLock.tryLock()) {
                    try {
                        return resetWindowTo(old, windowStart);
                    } finally {
                        updateLock.unlock();
                    }
                } else {
                    Thread.yield();
                }
            } else if (windowStart < old.windowStart()) {
                //时钟回拨了，一般不会走到这里
                return new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket(timeMillis));
            }
        }
    }
    public T getWindowValue(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        int idx = calculateTimeIdx(timeMillis);

        WindowWrap<T> bucket = array.get(idx);

        if (bucket == null || !bucket.isTimeInWindow(timeMillis)) {
            return null;
        }

        return bucket.value();
    }


    public int getSampleCount() {
        return sampleCount;
    }


    public int getIntervalInMs() {
        return intervalInMs;
    }


    public double getIntervalInSecond() {
        return intervalInMs / 1000.0;
    }

    public List<T> values() {
        return values(TimeUtil.currentTimeMillis());
    }

    public List<T> values(long timeMillis) {
        if (timeMillis < 0) {
            return new ArrayList<T>();
        }
        int size = array.length();
        List<T> result = new ArrayList<T>(size);

        for (int i = 0; i < size; i++) {
            WindowWrap<T> windowWrap = array.get(i);
            if (windowWrap == null || isWindowDeprecated(timeMillis, windowWrap)) {
                continue;
            }
            result.add(windowWrap.value());
        }
        return result;
    }


    public boolean isWindowDeprecated(long time, WindowWrap<T> windowWrap) {
        return time - windowWrap.windowStart() > intervalInMs;
    }

}
