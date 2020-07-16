package com.lcf.metric;

import com.lcf.data.MetricBucket;

import java.util.List;

/**
 * 统计流量数据的接口
 *
 * @author karthur
 * @date  2020.7.9
 */
public interface Metric {

    /**
     * Get total success count.
     *
     * @return success count
     */
    long success();

    /**
     * Get max success count.
     *
     * @return max success count
     */
    long maxSuccess();

    /**
     * Get total exception count.
     *
     * @return exception count
     */
    long exception();


    /**
     * Get total pass count.
     *
     * @return pass count
     */
    long pass();

    /**
     * Get total response time.
     *
     * @return total RT
     */
    long rt();

    /**
     * Get the minimal RT.
     *
     * @return minimal RT
     */
    long minRt();

    /**
     * Get the raw window array.
     *
     * @return window metric array
     */
    MetricBucket[] windows();

    /**
     * Add current exception count.
     *
     * @param n count to add
     */
    void addException(int n);

    /**
     * Add current completed count.
     *
     * @param n count to add
     */
    void addSuccess(int n);

    /**
     * Add current pass count.
     *
     * @param n count to add
     */
    void addPass(int n);

    /**
     * Add given RT to current total RT.
     *
     * @param rt RT
     */
    void addRT(long rt);

    void addReject(int n);

    long reject();

    /**
     * Get the sliding window length in seconds.
     *
     * @return the sliding window length
     */
    double getWindowIntervalInSec();

    /**
     * Get sample count of the sliding window.
     *
     * @return sample count of the sliding window.
     */
    int getSampleCount();

    /**
     * Note: this operation will not perform refreshing, so will not generate new buckets.
     *
     * @param timeMillis valid time in ms
     * @return pass count of the bucket exactly associated to provided timestamp, or 0 if the timestamp is invalid
     * @since 1.5.0
     */
    long getWindowPass(long timeMillis);


}
