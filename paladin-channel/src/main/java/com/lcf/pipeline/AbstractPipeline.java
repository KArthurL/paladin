package com.lcf.pipeline;

import com.lcf.channel.Channel;
import com.lcf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPipeline implements Pipeline {

    private static final Logger logger= LoggerFactory.getLogger(AbstractPipeline.class);
    private final Channel channel;

    private final Class<?> clazz;
    private final Object invoker;

    private Context head;
    private Context tail;


    protected AbstractPipeline(Channel channel, Class<?> clazz,Object invoker){
        this.channel=channel;
        this.clazz=clazz;
        this.invoker=invoker;
        head=newHead();
        tail=newTail();
        if(!Objects.isNull(head) && !Objects.isNull(tail)){
            head.setNext(tail);
            tail.setPre(head);
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public List<Context> getContexts() {
        List<Context> contexts=new ArrayList<>();
        Context start=head;
        while(start!=null){
            contexts.add(start);
            start=start.next();
        }
        return contexts;
    }

    @Override
    public void invoke(Object obj) {
        if(!Objects.isNull(head)) {
            head.invoke(obj);
        }else{
            logger.error(clazz+"'s pipeline has no head!");
        }
    }

    @Override
    public synchronized Pipeline addContext(String name, Context context){

        if(!Objects.isNull(context)){
            context.setName(name);
            Context last=tail.pre();
            last.setNext(context);
            context.setNext(tail);
            tail.setPre(context);
        }
        return this;
    }

    @Override
    public Pipeline addContext(Context context) {
        return addContext(null,context);
    }

    @Override
    public void response(Object obj) {
        if(!Objects.isNull(tail)){
            tail.onResponse(obj);
        }else{
            logger.error(clazz+"'s pipeline has no tail!");
        }
    }

    @Override
    public void exception(Object obj) {
        if(!Objects.isNull(tail)){
            tail.onException(obj);
        }else{
            logger.error(clazz+"'s pipeline has no tail!");
        }
    }

    public Object getInvoker(){
        return invoker;
    }




    protected abstract Context newHead();
    protected abstract Context newTail();

    @Override
    public Context getContext(Class<?> clazz) {
        if(clazz!=null){
            Context start=head;
            while(start!=null){
                if (clazz.isAssignableFrom(start.getClass())){
                    return start;
                }else{
                    start=start.next();
                }
            }
        }

        return null;
    }



}
interface test{

}
class a implements test{}