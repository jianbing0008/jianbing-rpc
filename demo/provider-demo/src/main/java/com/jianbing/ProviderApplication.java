package com.jianbing;

import com.jianbing.discovery.RegistryConfig;
import com.jianbing.impl.HelloRpcImpl;

public class ProviderApplication {
    public static void main(String[] args) {
        //服务提供方，需要注册服务，启动服务
        //1.封装要发布的服务
        ServiceConfig<HelloRpc> service = new ServiceConfig<>();
        service.setInterface(HelloRpc.class);
        service.setRef(new HelloRpcImpl());
        //2.定义注册中心

        //3.通过启动引导程序，启动服务提供方
        RpcBootstrap.getInstance()
                .application("first-rpc-provider")
                //配置注册信息
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                //协议
                .protocol(new ProtocolConfig("jdk"))
                //发布服务
                .publish(service)
                //启动服务
                .start();
    }
}
