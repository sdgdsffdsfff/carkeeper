package com.taobao.taokeeper.monitor.domain;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yijunzhang
 * Date: 13-12-25
 * Time: 下午3:00
 * To change this template use File | Settings | File Templates.
 */
public class KeeperNode {

    private String id;

    private String pid = "/";

    private String path;

    private String name;

    private String data;

    private boolean hasChild;

    private List<KeeperNode> childList;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.id = path;
        String[] array = path.split("/");
        if (array.length > 2) {
            this.pid = path.substring(0, path.lastIndexOf("/"));
        }
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<KeeperNode> getChildList() {
        return childList;
    }

    public void setChildList(List<KeeperNode> childList) {
        this.childList = childList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "KeeperNode{" +
                "id='" + id + '\'' +
                ", pid='" + pid + '\'' +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", childList=" + childList +
                '}';
    }

    public boolean getHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public static void main(String[] args) {
        KeeperNode keeperNode = new KeeperNode();
        keeperNode.setPath("/mobil");
        System.out.println(keeperNode.getPid());
    }
}
