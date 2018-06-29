package com.diversion.zk;

import com.diversion.container.Config;
import com.diversion.container.Initialization;
import com.diversion.node.DiversionCluster;
import com.diversion.transport.Charset;

import java.util.List;

/**
 * Content : zookeeper注册中心
 *
 * @author liou 2017-12-29.
 */
public class ZookeeperRegister implements Initialization {

    public static final String REGISTER_ZK_ROOT = "/diversion";

    private ZookeeperClient zookeeperClient;
    private String clusterPath;
    private DiversionCluster diversionCluster;

    @Config("io.charset")
    private Charset charset;

    public ZookeeperRegister() {
    }

    @Override
    public void init() throws Exception {
        clusterPath = zookeeperClient.createPersistent(null, REGISTER_ZK_ROOT, "diversion".getBytes());
        clusterPath = zookeeperClient.createPersistent(clusterPath, diversionCluster.getNamespace(), diversionCluster.getNamespace().getBytes());
        List<String> children = zookeeperClient.getChildren(clusterPath);
        diversionCluster.build(children);
        registerThis();
        new NodeWatcher(diversionCluster, zookeeperClient.getClient(), clusterPath);
    }

    /**
     * 在zookeeper注册本节点信息
     *
     * @throws Exception
     */
    private void registerThis() throws Exception {
        String localNodeString = diversionCluster.getLocalNodeString();
        zookeeperClient.createEphemeral(clusterPath, localNodeString, localNodeString.getBytes(charset.charset()));
    }

    public String getClusterPath() {
        return clusterPath;
    }

    public void setDiversionCluster(DiversionCluster diversionCluster) {
        this.diversionCluster = diversionCluster;
    }

    public void setZookeeperClient(ZookeeperClient zookeeperClient) {
        this.zookeeperClient = zookeeperClient;
    }

}
