package com.taobao.taokeeper.monitor.zk;

import org.apache.curator.framework.CuratorFramework;

/**
 * zk 监听接口
 * User: yijunzhang
 * Date: 13-11-18
 * Time: 上午11:42
 */
public interface ZkListener {

    /**
     * 执行监听操作
     * @param curatorFramework
     */
    public void execute(CuratorFramework curatorFramework);

    /**
     * 返回被监听znode路径
     * @return
     */
    public String getPath();
}
