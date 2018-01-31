package com.liou.diversion.zk;

import com.liou.diversion.container.Config;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.container.Initialization;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content:
 * 2017-12-27 @author liou.
 */
public class ZookeeperClient implements Initialization, Destroyable{

    private static Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private CuratorFramework client;

    @Config("diversion.zookeeper.servers")
    private String zkServers;
    @Config("diversion.zookeeper.sessiontimeout")
    private int zkSessionTimeout;
    @Config("diversion.zookeeper.connecttimeout")
    private int zkConnectionTimeout;
    @Config("diversion.zookeeper.attempts")
    private int zkMaxAttempts;

    public ZookeeperClient() {
    }

    @Override
    public void init() throws Exception {
        client = CuratorFrameworkFactory.builder().connectString(zkServers).sessionTimeoutMs(zkSessionTimeout)
                .connectionTimeoutMs(zkConnectionTimeout)
                .retryPolicy(new RetryNTimes(zkMaxAttempts, 1000)).build();
        client.getCuratorListenable().addListener(new ZookeeperListener());
        client.getConnectionStateListenable().addListener(new ConnecStateListener());
        client.getUnhandledErrorListenable().addListener(new ExceptionListener());
        client.start();
    }

    public String createPersistent(String parent, String path, byte[] data) throws Exception {
        if (StringUtils.isNotBlank(parent)) {
            path = parent + '/' + path;
            path.replaceAll("//", "/");
        }
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            return path;
        }
        return client.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path, data);
    }

    public String createEphemeral(String parent, String path, byte[] data) throws Exception {
        if (StringUtils.isNotBlank(parent)) {
            path = parent + '/' + path;
            path.replaceAll("//", "/");
        }
        return client.create().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path, data);
    }

    public boolean delete(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        } catch (Exception e) {
            logger.warn("delete node {}", path, e);
        }
        return false;
    }

    public CuratorFramework getClient() {
        return client;
    }

    @Override
    public void destroy() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
            }
        }
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public void setZkSessionTimeout(Integer zkSessionTimeout) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

    public void setZkConnectionTimeout(Integer zkConnectionTimeout) {
        this.zkConnectionTimeout = zkConnectionTimeout;
    }

    public void setZkMaxAttempts(Integer zkMaxAttempts) {
        this.zkMaxAttempts = zkMaxAttempts;
    }

}
