package com.jianbing;

public class Constant {
    // 默认的zookeeper连接地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    // 超时时间
    public static final int ZK_SESSION_TIMEOUT = 10000;

    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/rpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/rpc-metadata/consumers";
}
