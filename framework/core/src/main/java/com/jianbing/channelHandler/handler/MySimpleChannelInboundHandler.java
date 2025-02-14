package com.jianbing.channelHandler.handler;

import com.jianbing.RpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;

/**
 * 仅用来测试的类
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        String result = msg.toString(CharsetUtil.UTF_8);
        // 从全局挂起的请求中，寻找匹配的待处理的completableFuture, 并设置结果
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
