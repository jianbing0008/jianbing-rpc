package com.jianbing.discovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 提供bootstrap单例
 */

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
                        socketChannel.pipeline().addLast(null);
                    }
                });
    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
