package com.lcf.asyn;

import com.lcf.common.RpcResponse;

/**
 * @author k.arthur
 * @date 2020.7.14
 */
public interface AsyncCallBack {
    void success(Object response);
    void fail(Object response);
}
