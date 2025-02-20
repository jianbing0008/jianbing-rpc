package com.jianbing.loadBalancer;

import java.net.InetSocketAddress;

public interface Selector {
    /**
     * 根据负载均衡算法，选择一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();


    //todo 服务动态上下线
    void reBalance();
}
