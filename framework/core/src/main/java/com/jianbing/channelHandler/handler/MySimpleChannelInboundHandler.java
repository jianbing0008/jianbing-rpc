package com.jianbing.channelHandler.handler;

import com.jianbing.RpcBootstrap;
import com.jianbing.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 自定义的ChannelInboundHandler，用于处理接收到的RpcResponse
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 处理接收到的RpcResponse消息
     *
     * @param channelHandlerContext ChannelHandlerContext对象，提供了与Channel相关的一系列上下文信息
     * @param rpcResponse RpcResponse对象，包含了从服务端接收到的响应信息
     * @throws Exception 抛出异常时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 服务提供方,给予的结果
        Object returnValue = rpcResponse.getBody();
        // 从全局挂起的请求中，寻找匹配的待处理的completableFuture, 并设置结果
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()) {
            log.debug("以寻找到编号--->【{}】的completableFuture,成功处理响应结果", rpcResponse.getRequestId());
        }
    }
}
