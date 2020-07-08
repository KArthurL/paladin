package com.lcf.protobuffer;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author k.arthur
 * @date  2020.7.8
 */
public class ProtobufferSerializeUtil {

    private static final Logger logger= LoggerFactory.getLogger(ProtobufferSerializeUtil.class);
    private static LoadingCache<Class<?>, Schema<?>> cache= CacheBuilder.newBuilder()
            // 缓存刷新时间
            .refreshAfterWrite(10, TimeUnit.MINUTES)
            // 设置缓存个数
            .maximumSize(500)
            .build(new CacheLoader<Class<?>, Schema<?>>() {
                @Override
                public Schema<?> load(Class<?> aClass)  {
                    return RuntimeSchema.getSchema(aClass) ;
                }
            });


    private static Objenesis objenesis = new ObjenesisStd(true);



    private static <T> Schema<T> getSchema(Class<T> clazz) {// 这里的clazz代表的是一个类型参数
        //根据类型从缓存里获取schema
        Schema<T> schema = null;
        try {
            schema = (Schema<T>) cache.get(clazz);
        } catch (ExecutionException e) {
            logger.error("get schema from cache has error, class: %s",clazz);
        }

        return schema;
    }


    /**
     * 序列化  Java Object转成byte[]
     */
    public static <T> byte[] serializer(T obj) {
        //获取泛型对象的类型
        Class<T> clazz = (Class<T>) obj.getClass();
        // 创建LinkedBuffer对象
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            // 序列化,并返回序列化对象
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            logger.error("serialize false, Object: %s",obj);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化 将byte[]转成Java Object
     */
    public static <T> T deserializer(byte[] data, Class<T> clazz) {
        try {
            // 通过objenesis根据泛型对象实例化对象
            T obj = objenesis.newInstance(clazz);
            // 获取泛型对象的schema对象
            Schema<T> schema = getSchema(clazz);
            // 将字节数组中的数据反序列化到message对象
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
            // 返回反序列化对象
            return obj;
        } catch (Exception e) {
            logger.error("serialize false, Class: %s",clazz);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
