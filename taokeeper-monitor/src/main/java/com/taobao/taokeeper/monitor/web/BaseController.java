package com.taobao.taokeeper.monitor.web;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.service.ZookeeperConfigService;
import common.toolkit.java.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.taokeeper.dao.AlarmSettingsDAO;
import com.taobao.taokeeper.dao.SettingsDAO;
import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.monitor.service.ReportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Description: Base Controller
 *
 * @author yinshi.nc
 * @Date 2011-11-11
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ZooKeeperClusterDAO zooKeeperClusterDAO;

    @Autowired
    protected AlarmSettingsDAO alarmSettingsDAO;

    @Autowired
    protected SettingsDAO taoKeeperSettingsDAO;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected ZookeeperConfigService zookeeperConfigService;

    protected ZooKeeperCluster getZooKeeperCluster(String clusterId) {
        Map<Integer, ZooKeeperCluster> zooKeeperClusterMap = GlobalInstance.getAllZooKeeperCluster();
        ZooKeeperCluster zooKeeperCluster = null;
        try {
            if (!zooKeeperClusterMap.isEmpty()) {
                if (StringUtil.isBlank(clusterId)) {
                    ArrayList<Integer> clusterIds = new ArrayList<Integer>(zooKeeperClusterMap.keySet());
                    Collections.sort(clusterIds);
                    clusterId = clusterIds.get(clusterIds.size() - 1).toString();
                }
                if (StringUtils.isNotBlank(clusterId)) {
                    zooKeeperCluster = zooKeeperClusterMap.get(Integer.parseInt(clusterId));
                }

                if (null == zooKeeperCluster) {
                    zooKeeperCluster = zooKeeperClusterDAO.getZooKeeperClusterByCulsterId(Integer.parseInt(clusterId));
                }
                return zooKeeperCluster;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

}
