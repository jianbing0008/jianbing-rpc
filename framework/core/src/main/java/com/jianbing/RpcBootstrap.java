package com.jianbing;

import com.jianbing.utils.zookeeper.ZookeeperNode;
import com.jianbing.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

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

    // 维护一个zookeeper实例
    private ZooKeeper zooKeeper;

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
        //TODO： 以后考虑将zk与当前工程解耦，扩展更多的实现
        zooKeeper = ZookeeperUtil.createZooKeeper();

        this.registryConfig = registryConfig;
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

        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDERS_PATH +"/"+ service.getInterface().getName();
        //这个节点是一个持久节点
        if(!ZookeeperUtil.exists(zooKeeper, parentNode, null)){
            ZookeeperNode node = new ZookeeperNode(parentNode, "".getBytes());
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点


        if(log.isDebugEnabled()){
            log.debug("服务{},已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     * @param services 封装需要发布的服务的集合
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig> services) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
    }


    /**
     * ---------------------------服务调用方的相关api-----------------------------------------
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        return this;
    }

}