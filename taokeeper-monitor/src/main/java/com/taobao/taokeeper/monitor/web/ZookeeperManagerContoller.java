package com.taobao.taokeeper.monitor.web;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.domain.KeeperNode;
import common.toolkit.java.util.io.ServletUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: yijunzhang
 * Date: 13-12-25
 * Time: 下午1:59
 */
@Controller
@RequestMapping("")
public class ZookeeperManagerContoller extends BaseController {

    @RequestMapping("/manager/json/createKeeperNode.do")
    @ResponseBody
    public Map<String, Object> createKeeperNode(String path, String name, String value, String clusterId, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean isSuccess = false;
//        Object loginUserName = request.getAttribute("loginUserName");
        try {
//            if (loginUserName == null || StringUtils.isBlank(loginUserName.toString())) {
//                map.put("error", "请登录index系统");
//                return map;
//            }
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                map.put("error", "path不能为空");
                return map;
            }
            if (StringUtils.isBlank(name)) {
                map.put("error", "name不能为空");
                return map;
            }
            String newPath = null;
            if (path.substring(path.length() - 1).equals("/")) {
                newPath = path + name;
            } else {
                newPath = path + "/" + name;
            }
            Stat stat = curatorFramework.checkExists().forPath(newPath);
            if (stat != null) {
                map.put("error", "znode:" + newPath + " 已经存在");
                return map;
            }
            KeeperNode node = zookeeperConfigService.createNode(path, name, value, curatorFramework);
            if (node != null) {
                isSuccess = true;
                Map<String, Object> nodeMap = new LinkedHashMap<String, Object>();
                nodeMap.put("id", node.getId());
                nodeMap.put("name", node.getName());
                if (node.getHasChild()) {
                    nodeMap.put("isParent", true);
                } else {
                    nodeMap.put("isParent", false);
                }
                nodeMap.put("dataURL", "manager/json/getKeeperNodeData.do?clusterId=" + clusterId + "&path=" + node.getPath());
                map.put("data", nodeMap);
            }
        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
            map.put("error", e.getMessage());
        }
        map.put("success", isSuccess);
        return map;
    }

    @RequestMapping("/manager/json/deleteKeeperNode.do")
    @ResponseBody
    public Map<String, Object> deleteKeeperNode(String path, String clusterId, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean isSuccess = false;
//        Object loginUserName = request.getAttribute("loginUserName");
        try {
//            if (loginUserName == null || StringUtils.isBlank(loginUserName.toString())) {
//                map.put("error", "请登录index 系统");
//                return map;
//            }
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                map.put("error", "zooKeeper集群不存在.");
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                path = "/";
            }
            KeeperNode keeperNode = zookeeperConfigService.getChildKeeperNode(path, curatorFramework);
            if (keeperNode.getChildList() == null || keeperNode.getChildList().isEmpty()) {
                isSuccess = zookeeperConfigService.deleteNode(path, curatorFramework);
            } else {
                map.put("error", "存在子节点,不允许删除.");
            }
        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
            map.put("error", e.getMessage());
        }
        map.put("success", isSuccess);
        return map;
    }

    @RequestMapping("/manager/json/updateKeeperNodeData.do")
    @ResponseBody
    public Map<String, Object> updateKeeperNodeData(String path, String value, String clusterId, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean isSuccess = false;
//        Object loginUserName = request.getAttribute("loginUserName");
        try {
//            if (loginUserName == null || StringUtils.isBlank(loginUserName.toString())) {
//                map.put("error", "请登录index系统");
//                return map;
//            }
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                path = "/";
            }
            isSuccess = zookeeperConfigService.setNodeData(path, value, curatorFramework);
        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
            map.put("error", e.getMessage());
        }
        map.put("success", isSuccess);
        return map;
    }

    @RequestMapping("/manager/json/getKeeperNodeData.do")
    @ResponseBody
    public KeeperNode getKeeperNodeData(String path, String clusterId) {
        try {
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                path = "/";
            }
            KeeperNode keeperNode = zookeeperConfigService.getNodeData(path, curatorFramework);
            if (keeperNode == null) {
                return new KeeperNode();
            } else {
                return keeperNode;
            }

        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
        }
        return new KeeperNode();
    }

    @RequestMapping("/manager/json/childKeeperNodes.do")
    @ResponseBody
    public List<Map<String, Object>> getChildKeeperNode(String path, String clusterId) throws Exception {
        try {
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                path = "/";
            }
            KeeperNode keeperNode = zookeeperConfigService.getChildKeeperNode(path, curatorFramework);
            if (keeperNode != null && keeperNode.getChildList() != null) {
                List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
                for (KeeperNode node : keeperNode.getChildList()) {
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("id", node.getId());
                    map.put("name", node.getName());
                    if (node.getHasChild()) {
                        map.put("isParent", true);
                    } else {
                        map.put("isParent", false);
                    }
                    map.put("dataURL", "manager/json/getKeeperNodeData.do?clusterId=" + clusterId + "&path=" + node.getPath());
                    resultList.add(map);
                }
                return resultList;
            }
            return Collections.emptyList();
        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }


    @RequestMapping("/manager.do")
    public ModelAndView zooKeeperConfigPAGE(HttpServletRequest request, HttpServletResponse response, String clusterId, String path) throws Exception {
        Map<Integer, ZooKeeperCluster> zooKeeperClusterMap = GlobalInstance.getAllZooKeeperCluster();
        try {
            ZooKeeperCluster zooKeeperCluster = getZooKeeperCluster(clusterId);
            if (null == zooKeeperCluster) {
                ServletUtil.writeToResponse(response, "目前还没有这样的ZK集群<a href='zooKeeper.do?method=zooKeeperRegisterPAGE'><font color='red'> 加入监控</font></a>");
                return null;
            }
            CuratorFramework curatorFramework = zookeeperConfigService.getCuratorCuratorFramework(zooKeeperCluster);
            if (StringUtils.isBlank(path)) {
                path = "/";
            }

            KeeperNode keeperNode = zookeeperConfigService.getChildKeeperNode("/", curatorFramework);
            KeeperNode valueNode = zookeeperConfigService.getNodeData(path, curatorFramework);
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("clusterId", zooKeeperCluster.getClusterId());
            model.put("keeperNode", keeperNode);
            model.put("valueNode", valueNode);
            model.put("clusterName", zooKeeperCluster.getClusterName());
            model.put("zooKeeperClusterMap", zooKeeperClusterMap);
            model.put("timeOfUpdateZooKeeperStatusSet", GlobalInstance.timeOfUpdateZooKeeperStatusSet);

            return new ModelAndView("manager/zookeeperConfigPAGE", model);
        } catch (NumberFormatException e) {
            logger.error("不合法的clusterId：" + clusterId, e);
            ServletUtil.writeToResponse(response, "不合法的clusterId：" + clusterId);
        } catch (Exception e) {
            logger.error("Server error : " + e.getMessage(), e);
            ServletUtil.writeToResponse(response, "Server error: " + e.getMessage());
        }
        return null;
    }


}