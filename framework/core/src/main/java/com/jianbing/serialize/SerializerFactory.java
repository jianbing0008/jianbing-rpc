package com.jianbing.serialize;

import com.jianbing.serialize.impl.HessianSerializer;
import com.jianbing.serialize.impl.JdkSerializer;
import com.jianbing.serialize.impl.JsonSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂类，负责创建和管理序列化器实例
 */
public class SerializerFactory {

    public static final Map<String, SerializerWrapper> SERIALIZE_CACHE_BY_TYPE = new ConcurrentHashMap<>(8);
    public static final Map<Byte, SerializerWrapper> SERIALIZE_CACHE_BY_CODE = new ConcurrentHashMap<>(8);

    /**
     * 用于初始化序列化器缓存
     */
    static{
        // 初始化序列化器，并包装到SerializerWrapper中
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZE_CACHE_BY_TYPE.put("jdk", jdk);
        SERIALIZE_CACHE_BY_TYPE.put("json", json);
        SERIALIZE_CACHE_BY_TYPE.put("hessian", hessian);
        SERIALIZE_CACHE_BY_CODE.put((byte) 1, jdk);
        SERIALIZE_CACHE_BY_CODE.put((byte) 2, json);
        SERIALIZE_CACHE_BY_CODE.put((byte) 3, hessian);
    }

    /**
     * 根据序列化类型获取对应的序列化器包装类
     * @param serializeType 序列化类型，如"jdk"或"json"
     * @return 对应序列化类型的序列化器包装类实例，如果不存在则返回null
     */
    public static SerializerWrapper getSerializerWrapper(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZE_CACHE_BY_TYPE.get(serializeType);
        if(serializerWrapper == null){
            return SERIALIZE_CACHE_BY_TYPE.get(serializeType);
        }
        return serializerWrapper;
    }
    public static SerializerWrapper getSerializerWrapper(byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZE_CACHE_BY_CODE.get(serializeCode);
        if(serializerWrapper == null){
            return SERIALIZE_CACHE_BY_TYPE.get("jdk");
        }
        return serializerWrapper;
    }
}
