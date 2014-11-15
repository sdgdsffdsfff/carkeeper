package com.taobao.taokeeper.monitor.domain;

/**
 * Created with IntelliJ IDEA.
 * User: yijunzhang
 * Date: 13-12-26
 * Time: 下午5:25
 * To change this template use File | Settings | File Templates.
 */
public class UserKeeper {

    private int clusterId;

    private String name;

    private int status;

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean canWrite() {
        return status == 2;
    }

    public boolean canRead() {
        return status == 1 || status == 2;
    }
}
