package com.jianbing;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcBootstrap {
    /**
     * ---------------------------服务提供方的相关api-----------------------------------------
     */

    //RpcBootstrap是个单例，希望每个应用程序都只有一个实例

    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();
    private RpcBootstrap() { // 构造启动引导程序，需要配置一些信息，比如注册中心地址，服务端口号等

    }
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 设置应用名称
     * @param appName 应用名称
     * @return this 当前实例
     */
    public RpcBootstrap application(String appName) {
        return this;
    }

    /**
     * 设置注册中心信息
     * @param registryConfig 注册中心信息
     * @return this 当前实例
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议信息
     * @return this 当前实例
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if(log.isDebugEnabled()){
            log.debug("当前工程使用的序列化协议:{}", protocolConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务，将接口以及匹配的实现注册到服务中心
     * @param service 封装需要发布的服务
     * @return this 当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        if(log.isDebugEnabled()){
            log.debug("服务{},已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     * @param services 封装需要发布的服务的集合
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig> services) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
    }


    /**
     * ---------------------------服务调用方的相关api-----------------------------------------
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        return this;
    }

}