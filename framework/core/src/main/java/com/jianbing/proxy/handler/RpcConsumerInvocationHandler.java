package com.jianbing.proxy.handler;

import com.jianbing.RpcBootstrap;
import com.jianbing.discovery.NettyBootStrapInitializer;
import com.jianbing.discovery.Registry;
import com.jianbing.enumeration.RequestType;
import com.jianbing.excepetions.DiscoveryException;
import com.jianbing.excepetions.NetworkException;
import com.jianbing.transport.message.RequestPayload;
import com.jianbing.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 封装客户端通信的基础逻辑，每一个代理对象的远程调用都封装在了invoke中
**/
@Slf4j
@AllArgsConstructor
public class RpcConsumerInvocationHandler implements InvocationHandler {

    //此处需要一个注册中心和接口
    private final Registry registry;
    private final Class<?> interfaceRef;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // helloRpc.sayHi("你好");
        // 调用sayHi方法，事实上是调用了helloProxy.invoke()方法
        // 我们已经知道method(具体的方法)和args(参数列表)
//        log.info("method---->{}", method.getName());
//        log.info("args---->{}", args);

        // 1.发现服务，从注册中心寻找一个可用的服务
        InetSocketAddress address = registry.lookup(interfaceRef.getName());// 传入服务名,返回ip+端口
        log.info("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);

        // 2.尝试获取一个可用的通道
        Channel channel = getAvaliableChannel(address);
        if(log.isDebugEnabled()){
            log.debug("获取到可用的通道【{}】", channel);
        }

        // 3.封装报文
        /**
         * --------------------封装报文---------------------------
         */
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .returnType(method.getReturnType())
                .build();
        // todo: 需要对各种请求id和类型做处理
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(1L)
                .requestType(RequestType.REQUEST.getCode())
                .compressType((byte) 1)
                .serializeType((byte) 1)
                .requestPayload(requestPayload)
                .build();


        /**
         * --------------------异步策略---------------------------
         */

        // 4.写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        RpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);

        // 网络通信（Netty异步写入）
        // writeAndFlush 写出一个请求，这个请求的实例会进入pipeline执行出站一系列操作
        channel.writeAndFlush(rpcRequest).addListener( (ChannelFutureListener) promise -> {

            if(!promise.isSuccess()){
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        // 5.获得响应结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个可用的channel
     * @param address
     * @return
     */
    private Channel getAvaliableChannel(InetSocketAddress address) {
        // 尝试从缓存中获取一个可用的channel
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

            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException|TimeoutException|RuntimeException|ExecutionException e) {
                log.error("获取通道时发生异常", e);
                throw new DiscoveryException(e);
            }
            // 将新的channel放入缓存中
            RpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }

        if(channel == null){
            throw new NetworkException("获取通道时发生异常");
        }
        return channel;
    }
}
