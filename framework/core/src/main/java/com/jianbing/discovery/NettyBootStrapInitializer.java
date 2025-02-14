package com.jianbing.discovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供bootstrap单例
 */
@Slf4j
public class NettyBootStrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();

    private NettyBootStrapInitializer(){

    }

    static{
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class) // 选择初始化一个什么样的Channel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
                                log.info("客户端收到来自---服务端的消息：{}", msg.toString(io.netty.util.CharsetUtil.UTF_8));
                            }
                        });
                    }
                });
    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
