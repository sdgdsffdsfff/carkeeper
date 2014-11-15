package com.taobao.taokeeper.monitor.service.impl;

import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.domain.KeeperNode;
import com.taobao.taokeeper.monitor.service.ZookeeperConfigService;
import common.toolkit.java.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: yijunzhang
 * Date: 13-12-25
 * Time: 下午2:19
 * To change this template use File | Settings | File Templates.
 */
public class ZookeeperConfigServiceImpl implements ZookeeperConfigService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<String, CuratorFramework> curatorMap = new ConcurrentHashMap<String, CuratorFramework>();

    @Override
    public CuratorFramework getCuratorCuratorFramework(ZooKeeperCluster zooKeeperCluster) {
        CuratorFramework curatorFramework = curatorMap.get(String.valueOf(zooKeeperCluster.getClusterId()));
        if (curatorFramework == null) {
            String connectionString = StringUtils.join(zooKeeperCluster.getServerList(), ",");
            addCurator(String.valueOf(zooKeeperCluster.getClusterId()), connectionString);
        }

        return curatorMap.get(String.valueOf(zooKeeperCluster.getClusterId()));
    }

    @Override
    public KeeperNode getNodeData(String path, CuratorFramework curatorFramework) {
        KeeperNode keeperNode = new KeeperNode();
        keeperNode.setPath(path);

        try {
            byte[] bytes = curatorFramework.getData().forPath(path);
            if (bytes == null || bytes.length == 0) {
                return keeperNode;
            }
            String data = new String(bytes);
            keeperNode.setData(data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return keeperNode;
    }

    @Override
    public boolean setNodeData(String path, String value, CuratorFramework curatorFramework) {
        if (StringUtils.isBlank(value)) {
            value = "";
        }
        byte[] bytes = value.getBytes(Charset.defaultCharset());
        try {
            Stat stat = curatorFramework.setData().forPath(path, bytes);
            return stat != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    public KeeperNode createNode(String path, String name, String value, CuratorFramework curatorFramework) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        KeeperNode keeperNode = new KeeperNode();
        keeperNode.setName(name);

        String newPath = null;
        if (path.substring(path.length() - 1).equals("/")) {
            newPath = path + name;
        } else {
            newPath = path + "/" + name;
        }
        keeperNode.setPath(newPath);
        try {
            String returnPath = curatorFramework.create().forPath(newPath, value == null ? null : value.getBytes());
            return keeperNode;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deleteNode(String path, CuratorFramework curatorFramework) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        try {
            curatorFramework.delete().forPath(path);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public KeeperNode getChildKeeperNode(String path, CuratorFramework curatorFramework) {
        KeeperNode parentNode = new KeeperNode();
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        parentNode.setPath(path);
        parentNode.setName(getNameByPath(path));
        try {
            if (!path.equals("/")) {
                byte[] bytes = curatorFramework.getData().forPath(path);
                if (bytes != null && bytes.length > 0) {
                    parentNode.setData(new String(bytes));
                }
            }

            List<String> childList = curatorFramework.getChildren().forPath(path);
            if (childList != null || childList.size() > 0) {
                List<KeeperNode> keeperNodes = new ArrayList<KeeperNode>();
                for (String child : childList) {
                    KeeperNode childNode = new KeeperNode();
                    childNode.setName(child);
                    if (path.equals("/")) {
                        childNode.setPath(path + child);
                    } else {
                        childNode.setPath(path + "/" + child);
                    }

                    keeperNodes.add(childNode);
                }
                fillHasChild(keeperNodes, curatorFramework);
                parentNode.setChildList(keeperNodes);
                return parentNode;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return parentNode;
        }
        return parentNode;
    }

    private void addCurator(String clusterId, String connectionString) {
        // 重试间隔时间:2000，重试次数:5
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(2000, 5);

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(10000)
                .sessionTimeoutMs(10000)
                .build();
        curatorFramework.start();
        curatorMap.put(clusterId, curatorFramework);
    }

    private String getNameByPath(String path) {
        if (StringUtils.isBlank(path)) {
            return "/";
        }
        String[] array = path.split("/");
        if (array.length > 1) {
            return array[array.length - 1];
        }
        return "/";
    }

    private void fillHasChild(List<KeeperNode> nodeList, CuratorFramework curatorFramework) {
        if (nodeList == null || nodeList.isEmpty()) {
            return;
        }
        for (KeeperNode keeperNode : nodeList) {
            try {
                List<String> list = curatorFramework.getChildren().forPath(keeperNode.getPath());
                keeperNode.setHasChild(list != null && list.size() > 0);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }

    public static void main(String[] args) {
        String path = "/123/123/123s/";
        System.out.println(path.substring(path.length() - 1));

    }
}
