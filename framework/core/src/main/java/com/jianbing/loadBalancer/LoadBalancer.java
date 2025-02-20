package com.jianbing.loadBalancer;

import java.net.InetSocketAddress;

/**
 * 负载均衡器的接口
 */
public interface LoadBalancer {
    /**
     * 根据服务名选择一个服务
     * @param serviceName 服务名
     * @return 服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
