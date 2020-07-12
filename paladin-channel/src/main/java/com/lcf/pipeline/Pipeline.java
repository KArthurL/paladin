package com.lcf.pipeline;

import com.lcf.channel.Channel;
import com.lcf.context.Context;

import java.util.List;

/**
 * 服务端的调用抽象为pipeline
 */
public interface Pipeline {

    Channel getChannel();

    List<Context> getContexts();

    void invoke(Object obj);
    void response(Object obj);
    void exception(Object obj);

    Pipeline addContext(Context context);

    Pipeline addContext(String name, Context context);



}
