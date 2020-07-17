package com.lcf.annotation;

import com.lcf.constants.RpcConstans;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 适配spring的服务提供方注解
 * @author k.arthur
 * @date 2020.7.12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    String group () default "paladin";
    String version() default "1.0.0" ;
    int type() default RpcConstans.MUTI;
}
