package com.jianbing;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

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


        ZooKeeper zooKeeper;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.ZK_SESSION_TIMEOUT;

        try {
            zooKeeper = new ZooKeeper(connectString, timeout, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
            //定义节点和数据
            String basePath = "/rpc-metadata";
            String providersPath = basePath + "/providers";
            String consumersPath = basePath + "/consumers";

            if(zooKeeper.exists(basePath,null)==null){
                String result = zooKeeper.create(basePath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("创建根节点【{}】成功创建：",result);
            }
            if(zooKeeper.exists(providersPath,null)==null){
                String result = zooKeeper.create(providersPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("节点【{}】成功创建：",result);
            }
            if(zooKeeper.exists(consumersPath,null)==null){
                String result = zooKeeper.create(consumersPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("节点【{}】成功创建：",result);
            }


        } catch (IOException  | KeeperException |InterruptedException e) {
            log.error("创建基础目录时失败",e);
        }

    }

}
