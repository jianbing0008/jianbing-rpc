package com.jianbing.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class MyChannelHandlerForServer extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("服务端收到消息：" + buf.toString(StandardCharsets.UTF_8));
        // 可以通过ctx获取channel
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("Hello Client".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.close();
    }
}
