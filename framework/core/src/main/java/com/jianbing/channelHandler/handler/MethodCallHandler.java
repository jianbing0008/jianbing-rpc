package com.jianbing.channelHandler.handler;

import com.jianbing.RpcBootstrap;
import com.jianbing.ServiceConfig;
import com.jianbing.enumeration.ResponseCode;
import com.jianbing.transport.message.RequestPayload;
import com.jianbing.transport.message.RpcRequest;
import com.jianbing.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@Builder
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    public MethodCallHandler() {
        // 构造函数
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
       // 1、获取负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();

        // 2、根据负载内容调用方法
        Object result = callTargetMethod(requestPayload);

        if (log.isDebugEnabled()) {
            log.debug("请求---->【{}】已在服务端完成方法调用", rpcRequest.getRequestId());
        }

        // 3、封装响应
        RpcResponse rpcResponse = RpcResponse.builder()
                .requestId(rpcRequest.getRequestId())
                .code((ResponseCode.SUCCESS.getCode()))
                .serializeType(rpcRequest.getSerializeType())
                .compressType(rpcRequest.getCompressType())
                .body(result)
                .build();


        // 4、写出响应
        channelHandlerContext.channel().writeAndFlush(rpcResponse);

    }

    private Object callTargetMethod(RequestPayload requestPayload) {

        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parameterTypes = requestPayload.getParameterTypes();
        Object[] parameters = requestPayload.getParameters();

        // 寻找合适的类进行方法调用
        ServiceConfig<?> serviceConfig = RpcBootstrap.SERVICE_LIST.get(interfaceName);
        if(serviceConfig != null){
            // 获取实现类
            Object refImpl = serviceConfig.getRef();
            try {
                // 通过反射调用方法
                return refImpl.getClass().getMethod(methodName, parameterTypes).invoke(refImpl, parameters);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                log.error("调用【{}】请求的方法【{}】时发生异常", interfaceName, methodName, e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
