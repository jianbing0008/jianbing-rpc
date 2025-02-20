package com.jianbing.loadBalancer.impl;

import com.jianbing.RpcBootstrap;
import com.jianbing.discovery.Registry;
import com.jianbing.excepetions.LoadBalancerException;
import com.jianbing.loadBalancer.LoadBalancer;
import com.jianbing.loadBalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡实现类
 */
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer {
    // 服务注册中心
    private Registry registry;

    // 一个服务匹配一个Selector
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    public RoundRobinLoadBalancer() {
        // 初始化注册中心
        this.registry = RpcBootstrap.getInstance().getRegistry();
    }

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        // 从缓存中获取服务的选择器
        Selector selector = cache.get(serviceName);
        if(selector==null){
            // 如果缓存中没有，则从注册中心获取服务列表，并创建新的选择器
            List<InetSocketAddress> serviceList = this.registry.lookup(serviceName);
            selector = new RoundRobinSelector(serviceList);
            cache.put(serviceName,selector);
        }
        // 使用选择器获取下一个服务地址
        return selector.getNext();
    }

    // 轮询选择器实现类
    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            // 检查服务列表是否为空
            if (serviceList == null || serviceList.isEmpty()) {
                log.error("进行负载均衡选取节点时发现服务列表为空");
                throw new LoadBalancerException();
            }
            // 获取当前索引对应的服务地址
            InetSocketAddress address = serviceList.get(index.get());
            // 更新索引，以实现轮询
            if(index.get() == serviceList.size() - 1){
                index.set(0);
            }else{
                index.incrementAndGet();
            }
            return address;
        }

        @Override
        public void reBalance() {
            // 重新平衡方法，目前未实现
        }
    }

}
