package com.jianbing.serialize;

/**
 * 序列化器接口
 * 定义了序列化与反序列化的方法规范
 */
public interface Serializer {
    /**
     * 序列化方法
     * 将给定的对象转换为字节数组形式
     *
     * @param object 待序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化方法
     * 将给定的字节数组转换为指定类的对象
     *
     * @param bytes 待反序列化的字节数组
     * @param clazz 指定的类类型
     * @param <T> 泛型标记，表示任意类型
     * @return 反序列化后的对象实例
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
