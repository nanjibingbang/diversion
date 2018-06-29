package com.diversion.zk;

import com.diversion.container.Config;
import com.diversion.container.Destroyable;
import com.diversion.container.Initialization;
import com.diversion.transport.Charset;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Content:
 * 2017-12-27 @author liou.
 */
public class ZookeeperClient implements Initialization, Destroyable {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);

    private CuratorFramework client;

    @Config("zookeeper.servers")
    private String zkServers;
    @Config("zookeeper.sessiontimeout")
    private int zkSessionTimeout;
    @Config("zookeeper.connecttimeout")
    private int zkConnectionTimeout;
    @Config("zookeeper.attempts")
    private int zkMaxAttempts;
    @Config("io.charset")
    private Charset charset;

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

    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            logger.warn("get children for path{}", path, e);
        }
        return new ArrayList<>();
    }

    public boolean setData(String data, String path) {
        try {
            client.setData().forPath(path, data.getBytes(charset.charset()));
            return true;
        } catch (Exception e) {
            logger.warn("set data for path{}", path, e);
        }
        return false;
    }

    public String getData(String path) {
        try {
            byte[] bytes = client.getData().forPath(path);
            return new String(bytes, charset.charset());
        } catch (Exception e) {
            logger.warn("get data for path{}", path, e);
        }
        return null;
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

}
