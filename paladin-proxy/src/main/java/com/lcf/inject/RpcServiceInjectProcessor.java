package com.lcf.inject;

import com.lcf.ServiceDiscovery;
import com.lcf.annotation.RpcReference;
import com.lcf.invoker.PaladinInvoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
;import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

@Component
public class RpcServiceInjectProcessor implements BeanPostProcessor {


    @Autowired
    ServiceDiscovery serviceDiscovery;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> curClass = bean.getClass();
        Field[] fields = curClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)){
                RpcReference rpcReference=field.getAnnotation(RpcReference.class);
                String group=rpcReference.group();
                String version=rpcReference.version();
                Class beforeClass= (Class) field.getGenericType();
                String service = beforeClass.getName()+":"+version+"_"+group;
                Object o2= Proxy.newProxyInstance(beforeClass.getClassLoader(), new Class<?>[]{beforeClass}, new PaladinInvoker(beforeClass,service));
                try {
                    field.setAccessible(true);
                    field.set(bean, o2);
                    serviceDiscovery.discover(service);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return bean;
            }
        }
        return bean;
    }
}
