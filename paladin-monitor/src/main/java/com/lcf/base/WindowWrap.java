package com.lcf.base;

/**
 * 每个时间片的包装类
 *
 * @author karthur
 * @date 2020.7.9
 */
public class WindowWrap<T> {
    /**
     * 时间片的时间跨度，ms
     */
    private final long windowLengthInMs;

    /**
     * 时间片的开始时间，ms
     */
    private long windowStart;

    /**
     * 该时间片存放的数据
     */
    private T value;


    public WindowWrap(long windowLengthInMs, long windowStart, T value) {
        this.windowLengthInMs = windowLengthInMs;
        this.windowStart = windowStart;
        this.value = value;
    }

    public long windowLength() {
        return windowLengthInMs;
    }

    public long windowStart() {
        return windowStart;
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }


    public WindowWrap<T> resetTo(long startTime) {
        this.windowStart = startTime;
        return this;
    }


    public boolean isTimeInWindow(long timeMillis) {
        return windowStart <= timeMillis && timeMillis < windowStart + windowLengthInMs;
    }

    @Override
    public String toString() {
        return "WindowWrap{" +
                "windowLengthInMs=" + windowLengthInMs +
                ", windowStart=" + windowStart +
                ", value=" + value +
                '}';
    }
}
