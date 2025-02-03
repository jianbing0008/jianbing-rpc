package com.jianbing.utils.zookeeper;

import com.jianbing.Constant;
import com.jianbing.excepetions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Zookeeper工具类，提供连接Zookeeper服务器，创建节点等操作
 */
@Slf4j
public class ZookeeperUtil {
    /**
     * 默认方式创建zookeeper实例
     * @return ZooKeeper实例
     */
    public static ZooKeeper createZooKeeper() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.ZK_SESSION_TIMEOUT;

        return createZooKeeper(connectString, timeout);
    }

    /**
     * 根据指定的连接字符串和会话超时时间创建Zookeeper实例
     * @param connectString Zookeeper服务器的连接字符串
     * @param timeout 会话超时时间
     * @return ZooKeeper实例
     */
    public static ZooKeeper createZooKeeper(String connectString, int timeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
            return zooKeeper;
        } catch (IOException  | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建节点
     * @param zooKeeper zookeeper实例
     * @param node 节点信息
     * @param watcher  监听器
     * @param createMode 节点类型
     * @return true：创建成功    false：已存在   异常：抛出
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
        try {
            if(zooKeeper.exists(node.getNodePath(), watcher)==null){
                String result = zooKeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】成功创建",result);
                return true;
            }else {
                log.info("节点【{}】已经存在",node.getNodePath());
            }
            return false;
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时失败",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭Zookeeper实例
     * @param zooKeeper Zookeeper实例
     */
    public static void closeZooKeeper(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper实例时发生异常",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     * @param zooKeeper zookeeper实例
     * @param path 节点路径
     * @param watcher 监听器
     * @return true：存在  false：不存在
     */
    public static boolean exists(ZooKeeper zooKeeper, String path, Watcher watcher){
        try {
            return zooKeeper.exists(path,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在时发生异常",path,e);
            throw new ZookeeperException(e);
        }
    }
}
