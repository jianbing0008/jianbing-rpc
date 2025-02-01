package com.jianbing.impl;

import com.jianbing.HelloRpc;

public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi, consumer:"+ msg;
    }
}
