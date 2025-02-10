package com.jianbing.discovery;

import com.jianbing.Constant;
import com.jianbing.discovery.impl.NacosRegistry;
import com.jianbing.discovery.impl.ZookeeperRegistry;
import com.jianbing.excepetions.DiscoveryException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegistryConfig {
    // 定义连接的url: zookeeper：// 127.0.0.1:2181、nacos：// 127.0.0.1:8848
    private final String connectString;


    /**
     * 使用简单工厂
     * @return 具体的注册中心实例
     */
    public Registry getRegistry() {
        // 1. 获取注册中心类型
        String registryType = getRegistryType(connectString, true).toLowerCase();
        if(registryType.equals("zookeeper")){
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIMEOUT);
        }else if (registryType.equals("nacos") ){
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host, Constant.TIMEOUT);
        }
        throw new DiscoveryException("不支持的注册中心类型");
    }

    private String getRegistryType(String connectString, boolean ifType) {
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new IllegalArgumentException("给定的注册中心连接url不合法");
        }
        if(!ifType){
            return typeAndHost[1];
        }
        return typeAndHost[0];
    }
}
