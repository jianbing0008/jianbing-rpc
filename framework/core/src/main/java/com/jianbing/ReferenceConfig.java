package com.jianbing;

import com.jianbing.discovery.Registry;
import com.jianbing.excepetions.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceRef = interfaceConsumer;
    }

    /**
     * (代理设计模式)获取api接口的代理对象
     * @return 代理对象
     */
    public T get() {
        // 此处一定是使用动态代理完成了一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        // 此处的helloProxy就是代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // helloRpc.sayHi("你好");
                // 我们调用sayHi方法，事实上是调用了helloProxy.invoke()方法，即走进这个代码段中
                // 我们已经知道method(具体的方法)和args(参数列表)
                log.info("method---->{}", method.getName());
                log.info("args---->{}", args);

                // 1.发现服务，从注册中心寻找一个可用的服务
                InetSocketAddress address = registry.lookup(interfaceRef.getName());// 传入服务名,返回ip+端口
                log.info("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                // 2.使用netty连接服务器，发送：调用的服务名+方法名+参数列表，得到结果


                // 尝试从缓存中获取一个通道
                Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
                if(channel == null){// 建立一个新的channel
                    // 创建一个线程池：NioEventLoopGroup
                    NioEventLoopGroup group = new NioEventLoopGroup();
                    try {
                        // 创建一个客户端的引导程序
                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.group(group)
                                .channel(NioSocketChannel.class) // 选择初始化一个什么样的Channel
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        socketChannel.pipeline().addLast(null);
                                    }
                                });
                        //尝试连接服务器
                        channel = bootstrap.connect(address).sync().channel();
                        //缓存
                        RpcBootstrap.CHANNEL_CACHE.put(address, channel);

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(channel == null){
                    throw new NetworkException("获取通道时发生异常");
                }


                ChannelFuture channelFuture = channel.writeAndFlush(new Object());

                return null;
            }
        });
        return (T) helloProxy;
    }
}
