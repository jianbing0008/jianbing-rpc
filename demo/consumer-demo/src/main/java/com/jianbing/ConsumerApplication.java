package com.jianbing;

import com.jianbing.discovery.RegistryConfig;

public class ConsumerApplication {
    public static void main(String[] args) {
        //想尽一切办法获取代理对象，使用ReferenceConfig进行封装
        //reference一定有生成代理的模板方法
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);

        //代理做了些什么，
        // 1.连接注册中心
        // 2.拉去服务列表
        // 3.选择一个服务并建立链接
        // 4.发送请求，携带一些信息（接口名，参数列表，方法 名）
        RpcBootstrap.getInstance()
                .application("first-rpc-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        //获取一个代理对象
        HelloRpc helloRpc = reference.get();
        helloRpc.sayHi("你好");
    }
}
