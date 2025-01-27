package com.jianbing.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AppClient {
    public void run(){
        // 创建一个线程池：NioEventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 创建一个客户端的引导程序
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                    .channel(NioSocketChannel.class) // 选择初始化一个什么样的Channel
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MyChannelHandlerForClient());
                        }
                    });
            //尝试连接服务器
            ChannelFuture channelFuture = null;

                channelFuture = bootstrap.connect().sync();
                //获取channel，并且写出数据
                channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("Hello Netty".getBytes(StandardCharsets.UTF_8)));
                //阻塞直到channel关闭
                channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        new AppClient().run();
    }

}
