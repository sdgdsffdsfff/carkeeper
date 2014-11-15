package com.taobao.taokeeper.monitor.zk;

import com.taobao.taokeeper.monitor.util.UserAuthorityUtil;
import com.taobao.taokeeper.monitor.zk.ZkListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.ListenerContainer;
import org.apache.curator.framework.recipes.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: yijunzhang
 * Date: 13-12-26
 * Time: 下午5:19
 * To change this template use File | Settings | File Templates.
 */
public class UserAuthorityZkListener implements ZkListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String path;

    public UserAuthorityZkListener(String path) {
        this.path = path;
    }

    @Override
    public void execute(CuratorFramework curatorFramework) {
        //path子节点检测
        final PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, path, true);
        ListenerContainer<PathChildrenCacheListener> pathChildrenCacheListener = childrenCache.getListenable();
        if (pathChildrenCacheListener.size() == 0) {
            pathChildrenCacheListener.addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    PathChildrenCacheEvent.Type type = event.getType();
                    ChildData childData = event.getData();
                    if (type == null || childData == null) {
                        return;
                    }
                    String path = childData.getPath();
                    String name = UserAuthorityUtil.getPathName(path);
                    if (type.ordinal() == PathChildrenCacheEvent.Type.CHILD_UPDATED.ordinal()
                            || type.ordinal() == PathChildrenCacheEvent.Type.CHILD_ADDED.ordinal()) {
                        byte[] data = childData.getData();
                        if (data != null && data.length > 0) {
                            String str = new String(data);
                            UserAuthorityUtil.pushZkValue(name, str);
                        }
                    }
                }
            });

            try {
                childrenCache.start();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                try {
                    childrenCache.close();
                } catch (IOException e1) {
                    logger.error(path, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    public static void main(String[] args) {
        String path = "/123/123/absd/sdffg";
        String name = path.substring(path.lastIndexOf("/") + 1, path.length());
        System.out.println(name);

    }
}
