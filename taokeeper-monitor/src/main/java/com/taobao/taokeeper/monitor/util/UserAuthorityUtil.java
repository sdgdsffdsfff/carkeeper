package com.taobao.taokeeper.monitor.util;

import com.taobao.taokeeper.monitor.domain.UserKeeper;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: yijunzhang
 * Date: 13-12-26
 * Time: 下午5:27
 * To change this template use File | Settings | File Templates.
 */
public class UserAuthorityUtil {

    private static ConcurrentMap<String, List<UserKeeper>> userKeeperMap = new ConcurrentHashMap<String, List<UserKeeper>>();

    public static void addUserKeeper(UserKeeper uk) {
        if (userKeeperMap.containsKey(uk.getName())) {
            List<UserKeeper> list = userKeeperMap.get(uk.getName());
            for (UserKeeper userKeeper : list) {
                if (userKeeper.getClusterId() == uk.getClusterId()) {
                    userKeeper.setStatus(uk.getStatus());
                    return;
                }
            }
            list.add(uk);
            userKeeperMap.put(uk.getName(), list);
        } else {
            List<UserKeeper> list = new ArrayList<UserKeeper>();
            list.add(uk);
            userKeeperMap.put(uk.getName(), list);
        }
    }

    public static void removeUserKeeper(String userName) {
        userKeeperMap.remove(userName);
    }

    /**
     * 获取用户有权限的集群id ,-1表示没有 0 表示所有
     *
     * @return
     */
    public static int getClusterIdByName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return -1;
        }
        List<Integer> ids = getClusterIdsByName(userName);

        if (ids != null && ids.size() > 0) {
            int maxId = -1;
            for (Integer clusterId : ids) {
                if (clusterId == 0) {
                    return 0;
                } else if (maxId < clusterId) {
                    maxId = clusterId;
                }
            }
            return maxId;
        } else {
            return -1;
        }
    }

    private static List<Integer> getClusterIdsByName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return Collections.emptyList();
        }
        if (userKeeperMap.containsKey(userName) && userKeeperMap.get(userName).size() > 0) {
            List<UserKeeper> list = userKeeperMap.get(userName);
            Set<Integer> ids = new HashSet<Integer>();
            for (UserKeeper uk : list) {
                int clusterId = uk.getClusterId();
                ids.add(clusterId);
            }
            return new ArrayList<Integer>(ids);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 判断是否对clusterId有读权限
     *
     * @param userName
     * @param clusterId
     * @return
     */
    public static boolean hasAuthoritied(String userName, int clusterId) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        List<Integer> ids = getClusterIdsByName(userName);
        if (ids != null && ids.size() > 0) {
            for (Integer id : ids) {
                if (id.equals(0) || id.equals(clusterId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断是否对clusterId有写权限
     *
     * @param userName
     * @param clusterId
     * @return
     */
    public static boolean hasWriteAuthoritied(String userName, int clusterId) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        List<Integer> ids = getClusterIdsByName(userName);
        if (ids != null && ids.size() > 0) {
            for (Integer id : ids) {
                if (id.equals(0) || id.equals(clusterId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void pushZkValue(String name,String value){
        if(StringUtils.isBlank(name) || StringUtils.isBlank(value)){
            return;
        }
        String[] array = value.split(";");
        for (String zkStr : array) {
            String[] arr = zkStr.split(",");
            int clusterId = Integer.parseInt(arr[0]);
            int status = Integer.parseInt(arr[1]);
            UserKeeper uk = new UserKeeper();
            uk.setName(name);
            uk.setClusterId(clusterId);
            uk.setStatus(status);
            addUserKeeper(uk);
        }

    }

    public static String getPathName(String path){
        if(StringUtils.isBlank(path)){
            return null;
        }
        if(path.contains("/")){
            String name = path.substring(path.lastIndexOf("/")+1,path.length());
            return name;
        }else{
            return path;
        }
    }

}
