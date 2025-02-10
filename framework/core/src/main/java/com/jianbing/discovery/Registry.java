package com.jianbing.discovery;

import com.jianbing.ServiceConfig;

public interface Registry{
    /**
     * 注册服务
     * @param serviceConfig 服务配置
     */
    void register(ServiceConfig<?> serviceConfig);
}
