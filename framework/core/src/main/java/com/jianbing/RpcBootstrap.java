package com.jianbing;

import com.jianbing.channelHandler.handler.MethodCallHandler;
import com.jianbing.channelHandler.handler.RpcRequestDecoder;
import com.jianbing.channelHandler.handler.RpcResponseEncoder;
import com.jianbing.discovery.Registry;
import com.jianbing.discovery.RegistryConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcBootstrap {

    /**
     * ---------------------------服务提供方的相关api-----------------------------------------
     */

    //RpcBootstrap是个单例，希望每个应用程序都只有一个实例
    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;

    // 注册中心
    private Registry registry;

    // 连接的缓存, 对象为key,要看是否重写equals和toString
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    // 维护一个服务列表，用于记录所有暴露的服务 key:interface的全限定名 value:ServiceConfig
    public static final Map<String,ServiceConfig<?>> SERVICE_LIST = new HashMap<>(16);

    // 维护一个对外挂起的 CompletableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    // 维护一个zookeeper实例
    //private ZooKeeper zooKeeper;

    private RpcBootstrap() { // 构造启动引导程序，需要配置一些信息，比如注册中心地址，服务端口号等
    }
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 设置应用名称
     * @param applicationName 应用名称
     * @return this 当前实例
     */
    public RpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 设置注册中心信息
     * @param registryConfig 注册中心信息
     * @return this 当前实例
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        // 维护一个zk实例，但是这样写就会将zk与当前工程耦合，考虑后续优化

        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议信息
     * @return this 当前实例
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if(log.isDebugEnabled()){
            log.debug("当前工程使用的序列化协议:{}", protocolConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务，将接口以及匹配的实现注册到服务中心
     * @param service 封装需要发布的服务
     * @return this 当前实例
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        // 抽象了服务注册，具体注册逻辑在registry中实现
        registry.register(service);
        // 维护一个服务列表，用于记录所有暴露的服务
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布
     * @param services 封装需要发布的服务的集合
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        // 1、创建EventLoop，老板只负责接受客户端连接，worker负责处理客户端的请求
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();// 默认是cpu核数*2

        try {
            // 2、需要服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3、设置服务器
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //核心，在这里添加很多入站和出战的handler
                            socketChannel.pipeline().addLast(new LoggingHandler()) //日志
                                                    // 请求解码
                                                    .addLast(new RpcRequestDecoder())
                                                    // 根据请求进行方法调用
                                                    .addLast(new MethodCallHandler())
                                                    // 响应编码
                                                    .addLast(new RpcResponseEncoder())
                            ;
                        }
                    });
            //4、绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 5、优雅的关闭
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ---------------------------服务调用方的相关api-----------------------------------------
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        // 1.reference 需要一个注册中心
        reference.setRegistry(registry);
        return this;
    }

}