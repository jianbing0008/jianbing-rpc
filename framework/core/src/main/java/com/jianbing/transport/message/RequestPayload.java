package com.jianbing.transport.message;

/**
 * 请求调用方所请求的接口方法信息
 * helloRpc.sayHi("你好");
 */
public class RequestPayload {
    // 请求的接口名称 -- com.jianbing.HelloRpc
    private String interfaceName;

    // 请求的方法名称 -- sayHi
    private String methodName;

    // 请求的参数类型 -- [java.lang.String]
    private Class<?>[] parameterTypes;  // 用于确定重载方法

    // 请求的参数 -- ["你好"]
    private Object[] parameters; // 用来执行方法调用

    // 返回值类型 -- [java.lang.String]
    private Class<?>[] returnTypes;

}
