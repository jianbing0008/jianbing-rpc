package com.jianbing.discovery;

import com.jianbing.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

public interface Registry{
    /**
     * 注册服务
     * @param serviceConfig 服务配置
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取服务列表
     * @param serviceName 服务名称
     * @return 服务地址列表（ip+端口）
     */
    List<InetSocketAddress> lookup(String serviceName);
}
