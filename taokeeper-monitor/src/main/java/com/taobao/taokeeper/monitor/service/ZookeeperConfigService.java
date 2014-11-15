package com.taobao.taokeeper.monitor.service;

import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.domain.KeeperNode;
import org.apache.curator.framework.CuratorFramework;

/**
 * User: yijunzhang
 * Date: 13-12-25
 * Time: 下午2:16
 */
public interface ZookeeperConfigService {

    /**
     * 获取CuratorFramework映射
     *
     * @return
     */
    public CuratorFramework getCuratorCuratorFramework(ZooKeeperCluster zooKeeperCluster);

    /**
     * 获取keeperNode及子节点
     * @param path
     * @param curatorFramework
     * @return
     */
    public KeeperNode getChildKeeperNode(String path, CuratorFramework curatorFramework);

    /**
     * 获取path节点数据
     * @param path
     * @param curatorFramework
     * @return
     */
    public KeeperNode getNodeData(String path, CuratorFramework curatorFramework);

    /**
     * 设置path节点数据
     * @param path
     * @param curatorFramework
     * @return
     */
    public boolean setNodeData(String path, String value, CuratorFramework curatorFramework);

    /**
     * 添加znode
     * @param path
     * @param curatorFramework
     * @return
     */
    public KeeperNode createNode(String path, String name,String value, CuratorFramework curatorFramework);

    /**
     * 删除 znode
     * @param path
     * @param curatorFramework
     * @return
     */
    public boolean deleteNode(String path, CuratorFramework curatorFramework);

}
