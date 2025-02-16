package com.jianbing.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.jianbing.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        if(log.isDebugEnabled()){
            log.debug("对象【{}】使用JSON序列化完成", object);
        }
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        // 启用 SupportClassForName 特性
        JSONReader.Context context = new JSONReader.Context(JSONReader.Feature.SupportClassForName);

        if(log.isDebugEnabled()){
            log.debug("类【{}】使用JSON反序列化完成", clazz);
        }

        return JSON.parseObject(bytes, clazz, context);
    }
}
