package com.jianbing.discovery;

import com.jianbing.ServiceConfig;

import java.net.InetSocketAddress;

public interface Registry{
    /**
     * 注册服务
     * @param serviceConfig 服务配置
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务名称
     * @return 服务地址（ip+端口）
     */
    InetSocketAddress lookup(String serviceName);
}
