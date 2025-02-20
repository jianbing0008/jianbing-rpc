package com.jianbing.discovery.impl;

import com.jianbing.Constant;
import com.jianbing.ServiceConfig;
import com.jianbing.discovery.AbstractRegistry;
import com.jianbing.utils.NetUtils;
import com.jianbing.utils.zookeeper.ZookeeperNode;
import com.jianbing.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosRegistry extends AbstractRegistry {
    // 维护一个zk实例
    private ZooKeeper zooKeeper;

    public NacosRegistry() {
        this.zooKeeper = ZookeeperUtil.createZooKeeper();
    }
    public NacosRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZooKeeper(connectString, timeout);
    }

    ;
    @Override
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点
        String parentNodePath = Constant.BASE_PROVIDERS_PATH +"/"+ service.getInterface().getName();
        //这个节点是一个持久节点
        if(!ZookeeperUtil.exists(zooKeeper, parentNodePath, null)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(parentNodePath, "".getBytes()), null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点,ip:port
        //服务提供方(netty)的端口一般自己设定，我们还需要一个获取ip的方法
        //ip我们通常是需要一个局域网ip,不是127.0.0.1，也不是ipv6的
        //如：192.168.1.1:8088
        //todo: 后续处理端口的问题
        String nodePath = parentNodePath + "/" + NetUtils.getIp() + ":" + 8088;
        if(!ZookeeperUtil.exists(zooKeeper, nodePath, null)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(nodePath, "".getBytes()), null, CreateMode.EPHEMERAL);
        }

        log.info("服务{},已经被注册", service.getInterface().getName());
    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return null;
    }
}
