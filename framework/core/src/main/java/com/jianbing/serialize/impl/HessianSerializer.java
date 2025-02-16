package com.jianbing.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.jianbing.excepetions.SerializeException;
import com.jianbing.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 使用JDK自带的序列化机制实现的序列化器
 */
@Slf4j
public class HessianSerializer implements Serializer {
    /**
     * 序列化给定的对象
     *
     * @param object 待序列化的对象
     * @return 对象的序列化字节数组表示
     */
    @Override
    public byte[] serialize(Object object) {
        if(object == null) {
            return null;
        }
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ){
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if(log.isDebugEnabled()){
                log.debug("对象【{}】使用hessian序列化完成", object);
            }
            return baos.toByteArray();
        }catch (IOException e) {
            log.error("使用hessian序列化对象时【{}】时发生异常", object);
            throw new SerializeException(e);
        }
    }

    /**
     * 反序列化给定的字节数组，将其转换回对象
     *
     * @param bytes 对象的序列化字节数组表示
     * @param clazz 期望的对象类型
     * @param <T> 泛型参数，表示对象类型
     * @return 反序列化后的对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null) {
            return null;
        }
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ){
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            if(log.isDebugEnabled()){
                log.debug("类【{}】使用hessian反序列化完成", clazz);
            }
            return (T)hessian2Input.readObject();
        }catch (IOException e) {
            log.error("使用hessian反序列化对象【{}】时发生异常", clazz);
            throw new SerializeException(e);
        }
    }
}
