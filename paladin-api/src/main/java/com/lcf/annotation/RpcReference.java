package com.lcf.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *适配spring的服务消费方，在component组件中的成员变量上直接标注即可
 * @author k.arthur
 * @date 2020.7.12
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    String group () default "paladin";
    String version() default "1.0.0" ;
}
