package com.jianbing;

import com.jianbing.discovery.Registry;
import com.jianbing.proxy.handler.RpcConsumerInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceRef = interfaceConsumer;
    }

    /**
     * (代理设计模式)获取api接口的代理对象
     * @return 代理对象
     */
    public T get() {
        // 此处一定是使用动态代理完成了一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        InvocationHandler handler = new RpcConsumerInvocationHandler(registry,interfaceRef);
        // 此处的helloProxy就是代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }
}
