package com.lcf.context;

import com.lcf.pipeline.Pipeline;

/**
 * pipeline上的节点
 * @author k.arthur
 * @date 2020.7.12
 */
public interface Context {

    void invoke(Object obj);

    void onResponse(Object obj);

    void onException(Object obj);

    Context next();

    Context pre();

    void setNext(Context context);
    void setPre(Context context);


    Pipeline getPipeline();

    void setPipeline(Pipeline pipeline);

    String getName();
    void setName(String name);
}
