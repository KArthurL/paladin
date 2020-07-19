package com.lcf.context;

import com.lcf.pipeline.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Context的抽象类，对用户屏蔽了链路的调用，只需要实现Context本身的功能即可
 * @author k.arthur
 * @date 2020.7.12
 */
public abstract class AbstractContext implements  Context {

    protected static Logger logger= LoggerFactory.getLogger(AbstractContext.class);

    private  Pipeline pipeline;

    private  String name;

    private Context pre;
    private Context next;

    protected AbstractContext(){
        this(null,null);
    };

    protected AbstractContext(Pipeline pipeline){
        this(null,pipeline);
    }

    protected AbstractContext(String name,Pipeline pipeline){
        if(StringUtils.isEmpty(name)){
            this.name=this.getClass().getName();
        }else{
            this.name=name;
        }
        this.pipeline=pipeline;
    }

    @Override
    public void invoke(Object obj) {
        boolean isSuccess=true;
        if(obj!=null){
            try {
                this. handle(obj);
            }catch (Exception e){
                isSuccess=false;
                logger.error(name+" handle message has error!"+e.getCause());
                if(!Objects.isNull(pre)){
                    this.onException(e);
                }
            }
            if(isSuccess && !Objects.isNull(next)){
                next.invoke(obj);
            }
        }
    }

    @Override
    public void onResponse(Object obj) {
        boolean isSuccess=true;
        if(obj!=null){
            try {
                this.response(obj);
            }catch (Exception e){
                isSuccess=false;
                logger.error(name+" handle response has error!",e.getCause());
                if(!Objects.isNull(pre)){
                    this.onException(e);
                }
            }
            if(isSuccess && !Objects.isNull(pre)){
                pre.onResponse(obj);
            }
        }
    }

    @Override
    public void onException(Object obj) {
        if(obj!=null){
            try {
                this.caughtException(obj);
                if(!Objects.isNull(pre)){
                    pre.onException(obj);
                }
            }catch (Exception e){

                logger.error(name+" handle response has error!"+e.getCause());
                if(!Objects.isNull(pre)){
                    pre.onException(e);
                }
            }

        }
    }


    /**
     * 自定义Context需实现的方法
     * @param obj
     */
    protected abstract void handle(Object obj);
    protected abstract void response(Object obj);
    protected abstract void caughtException(Object obj);


    @Override
    public void setPre(Context pre) {
        this.pre = pre;
    }


    @Override
    public void setNext(Context next) {
        this.next = next;
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        this.pipeline=pipeline;
    }

    @Override
    public Context next() {
        return next;
    }

    @Override
    public Context pre() {
        return pre;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if(!StringUtils.isEmpty(name)) {
            this.name = name;
        }
    }
}
