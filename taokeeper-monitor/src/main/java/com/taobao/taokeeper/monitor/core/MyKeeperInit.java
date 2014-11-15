package com.taobao.taokeeper.monitor.core;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.common.SystemInfo;
import com.taobao.taokeeper.common.constant.SystemConstant;
import com.taobao.taokeeper.dao.SettingsDAO;
import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.model.TaoKeeperSettings;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.util.UserAuthorityUtil;
import com.taobao.taokeeper.monitor.zk.UserAuthorityZkListener;
import com.taobao.taokeeper.monitor.zk.ZkListener;
import common.toolkit.java.exception.DaoException;
import common.toolkit.java.util.ObjectUtil;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.db.DbcpUtil;
import common.toolkit.java.util.number.IntegerUtil;
import common.toolkit.java.util.system.SystemUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;

/**
 * User: yijunzhang
 * Date: 13-12-26
 * Time: 下午4:53
 */
public class MyKeeperInit {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String path;

    private CuratorFramework zkClient;

    private ZkListener zkListener;

    public final static String author = "vrs-zookeeper";

    @Resource(name = "zooKeeperClusterDAO")
    private ZooKeeperClusterDAO zooKeeperClusterDAO;

    @Resource(name = "taoKeeperSettingsDAO")
    private SettingsDAO taoKeeperSettingsDAO;

    private void initAuthorUser() {
        Properties properties = null;
        try {
            properties = SystemUtil.loadProperty();
            if (ObjectUtil.isBlank(properties))
                throw new Exception("Please defined,such as -DconfigFilePath=\"path/config-online.properties\"");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        String baseZkAddress = properties.getProperty("base.zk.address");
        path = properties.getProperty("base.zk.path.user");
        zkListener = new UserAuthorityZkListener(path);

        // 重试间隔时间:2000，重试次数:5
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(2000, 5);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(baseZkAddress)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(10000)
                .sessionTimeoutMs(10000)
                .build();

        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("CuratorFramework state changed: {}", newState);
                if (newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED) {
                    //确保监听路径存在
                    ensure(zkListener.getPath(), client);
                    //执行注册listner操作(RECONNECTED事件会导致重复注册.)
                    zkListener.execute(client);
                    logger.info("Listener {} executed!", zkListener.getClass().getName());
                }
            }
        });

        zkClient.start();

        //初始化作者
        try {
            String authorPath = path + "/" + author;
            Stat stat = zkClient.checkExists().forPath(authorPath);
            if (stat == null) {
                zkClient.create().forPath(path + "/" + author, "0,2".getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化作者失败:" + e.getMessage(), e);
        }
        //初始化权限数据
        try {
            List<String> childList = zkClient.getChildren().forPath(path);
            if (childList != null && childList.size() > 0) {
                for (String child : childList) {
                    String newPath = path + "/" + child;
                    byte[] data = zkClient.getData().forPath(newPath);
                    if (data != null && data.length > 0) {
                        String value = new String(data);
                        String name = child;
                        UserAuthorityUtil.pushZkValue(name, value);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化权限数据失败:" + e.getMessage(), e);
        }
    }

    public void init() {
        initSystem();

        initAuthorUser();

        initZooKeeperClusterMap();
    }

    /**
     * 从数据库加载并初始化系统配置
     */
    public void initSystem() {
        logger.info("=================================Start to init system===========================");
        Properties properties = null;
        try {
            properties = SystemUtil.loadProperty();
            if (ObjectUtil.isBlank(properties))
                throw new Exception("Please defined,such as -DconfigFilePath=\"W:\\TaoKeeper\\taokeeper\\config\\config-test.properties\"");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        SystemInfo.envName = StringUtil.defaultIfBlank(properties.getProperty("systemInfo.envName"), "TaoKeeper-Deploy");

        DbcpUtil.driverClassName = StringUtil.defaultIfBlank(properties.getProperty("dbcp.driverClassName"), "com.mysql.jdbc.Driver");
        DbcpUtil.dbJDBCUrl = StringUtil.defaultIfBlank(properties.getProperty("dbcp.dbJDBCUrl"), "jdbc:mysql://127.0.0.1:3306/taokeeper");
        DbcpUtil.characterEncoding = StringUtil.defaultIfBlank(properties.getProperty("dbcp.characterEncoding"), "UTF-8");
        DbcpUtil.username = StringUtil.trimToEmpty(properties.getProperty("dbcp.username"));
        DbcpUtil.password = StringUtil.trimToEmpty(properties.getProperty("dbcp.password"));
        DbcpUtil.maxActive = IntegerUtil.defaultIfError(properties.getProperty("dbcp.maxActive"), 30);
        DbcpUtil.maxIdle = IntegerUtil.defaultIfError(properties.getProperty("dbcp.maxIdle"), 10);
        DbcpUtil.maxWait = IntegerUtil.defaultIfError(properties.getProperty("dbcp.maxWait"), 10000);

        SystemConstant.dataStoreBasePath = StringUtil.defaultIfBlank(properties.getProperty("SystemConstent.dataStoreBasePath"),
                "/opt/taokeeper-monitor/");
        SystemConstant.userNameOfSSH = StringUtil.defaultIfBlank(properties.getProperty("SystemConstant.userNameOfSSH"), "");
        SystemConstant.passwordOfSSH = StringUtil.defaultIfBlank(properties.getProperty("SystemConstant.passwordOfSSH"), "");
        SystemConstant.portOfSSH = IntegerUtil.defaultIfError(properties.getProperty("SystemConstant.portOfSSH"), 22);

        logger.info("=================================Finish init system===========================");

        TaoKeeperSettings taoKeeperSettings = null;
        try {
            taoKeeperSettings = taoKeeperSettingsDAO.getTaoKeeperSettingsBySettingsId(1);
        } catch (DaoException e) {
            e.printStackTrace();
        }
        if (null != taoKeeperSettings)
            GlobalInstance.taoKeeperSettings = taoKeeperSettings;
    }

    private void initZooKeeperClusterMap() {
        try {
            List<ZooKeeperCluster> list = zooKeeperClusterDAO.getAllDetailZooKeeperCluster();
            if (list != null && list.size() > 0) {
                for (ZooKeeperCluster zkCluster : list) {
                    GlobalInstance.putZooKeeperCluster(zkCluster.getClusterId(), zkCluster);
                }
            }
        } catch (DaoException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean ensure(String path, CuratorFramework zkClient) {
        EnsurePath ensurePath = new EnsurePath(path);
        try {
            ensurePath.ensure(zkClient.getZookeeperClient());
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

}
