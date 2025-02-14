package com.jianbing.discovery;

import com.jianbing.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
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
                .handler(new ConsumerChannelInitializer());
    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
