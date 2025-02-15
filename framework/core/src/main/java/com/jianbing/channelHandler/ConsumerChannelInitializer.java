package com.jianbing.channelHandler;

import com.jianbing.channelHandler.handler.MySimpleChannelInboundHandler;
import com.jianbing.channelHandler.handler.RpcMessageEncoder;
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
                .addLast(new RpcMessageEncoder())
                .addLast(new MySimpleChannelInboundHandler()); //入站测试用的，留着备用
    }
}
