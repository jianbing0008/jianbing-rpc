package com.jianbing;

import com.jianbing.discovery.NettyBootStrapInitializer;
import com.jianbing.discovery.Registry;
import com.jianbing.excepetions.NetworkException;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
                    CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
                    NettyBootStrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                        if(promise.isDone()){
                            //异步完成
                            if(log.isDebugEnabled()){
                                log.debug("连接服务器【{}】成功", address);
                            }
                            channelFuture.complete(promise.channel());
                        }else if(!promise.isSuccess()){
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });

                    channel = channelFuture.get(3, TimeUnit.SECONDS);
                    // 将新的channel放入缓存中
                    RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }

                if(channel == null){
                    throw new NetworkException("获取通道时发生异常");
                }


                /**
                 * --------------------封装报文---------------------------
                 */



                /**
                 * --------------------同步策略---------------------------
                 */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object());
//                if (channelFuture.isDone()){
//                    Object now = channelFuture.getNow();
//                }else if(!channelFuture.isSuccess()){
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }



                /**
                 * --------------------异步策略---------------------------
                 */

                // 创建异步结果容器
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                RpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);

                // 网络通信（Netty异步写入）
                channel.writeAndFlush(Unpooled.copiedBuffer("--Hello--server--".getBytes(StandardCharsets.UTF_8))).addListener( (ChannelFutureListener) promise -> {

                    if(!promise.isSuccess()){
                    completableFuture.completeExceptionally(promise.cause());
                    }
                });
                // 阻塞等待结果（带超时）
                return completableFuture.get(10, TimeUnit.SECONDS);
            }
        });
        return (T) helloProxy;
    }
}
