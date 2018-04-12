package com.liou.diversion.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.WatchedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Content : zookeeper节点监控
 *
 * @author liou 2017-12-28.
 */
public class NodeWatcher implements CuratorWatcher {

    private CuratorFramework client;
    private List<String> children;
    private ChildrenChangeHandler childrenChangeHandler;
    private String parentPath;

    public NodeWatcher(ChildrenChangeHandler childrenChangeHandler, CuratorFramework client, String parent) throws Exception {
        this.client = client;
        this.childrenChangeHandler = childrenChangeHandler;
        this.parentPath = parent;
        children = autoWatch();
    }

    public List<String> autoWatch() throws Exception {
        if (CuratorFrameworkState.STARTED == client.getState()) {
            List<String> result = client.getChildren().usingWatcher(this).forPath(parentPath);
            return result == null ? new ArrayList<>() : result;
        }
        return children;
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            List<String> currents = autoWatch();
            for (String child : children) {
                if (!currents.contains(child)) {
                    childrenChangeHandler.handleChildRemoved(child);
                }
            }
            for (String child : currents) {
                if (!children.contains(child)) {
                    childrenChangeHandler.handleChildAdded(child);
                }
            }
            children = currents;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
