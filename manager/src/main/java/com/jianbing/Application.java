package com.jianbing;

import com.jianbing.utils.zookeeper.ZookeeperNode;
import com.jianbing.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * 注册中心的管理页面
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        // 帮我们创建基础目录
        // rpc-metadata   (持久节点)
        //  └─ providers （持久节点）
        //  		└─ service1  （持久节点，接口的全限定名）
        //  		    ├─ node1 [data]     /ip:port
        //  		    ├─ node2 [data]
        //            └─ node3 [data]
        //  └─ consumers
        //        └─ service1
        //             ├─ node1 [data]
        //             ├─ node2 [data]
        //             └─ node3 [data]
        //  └─ config

        // 创建 ZooKeeper 客户端实例
        ZooKeeper zooKeeper = ZookeeperUtil.createZooKeeper();

        // 定义基础路径常量
        String basePath = "/rpc-metadata";
        String providersPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";

        // 创建基础节点实例
        ZookeeperNode baseNode = new ZookeeperNode(basePath, "".getBytes());
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, "".getBytes());
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, "".getBytes());

        // 遍历基础节点列表，创建持久节点
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });

        // 关闭 ZooKeeper 客户端连接
        ZookeeperUtil.closeZooKeeper(zooKeeper);

    }

}
