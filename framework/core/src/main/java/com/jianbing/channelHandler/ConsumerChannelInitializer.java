package com.jianbing.channelHandler;

import com.jianbing.channelHandler.handler.MySimpleChannelInboundHandler;
import com.jianbing.channelHandler.handler.RpcRequestEncoder;
import com.jianbing.channelHandler.handler.RpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 自定义消息编码器
                .addLast(new RpcRequestEncoder())
                // 入站的消息响应解码器
                .addLast(new RpcResponseDecoder())
                // 处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
