package com.liou.diversion.zk;

import com.liou.diversion.container.Initialization;
import com.liou.diversion.node.DiversionCluster;

/**
 * Content : zookeeper注册中心
 *
 * @author liou 2017-12-29.
 */
public class ZookeeperRegister implements Initialization {

    private ZookeeperClient zookeeperClient;
    private String clusterPath;

    private DiversionCluster diversionCluster;

    public ZookeeperRegister() {
    }

    @Override
    public void init() throws Exception {
        clusterPath = zookeeperClient.createPersistent(null, diversionCluster.getPath(), "diversion_cluster_data".getBytes());
        registerThis();
        new NodeWatcher(diversionCluster, zookeeperClient.getClient());
    }

    private void registerThis() throws Exception {
        zookeeperClient.createEphemeral(clusterPath, diversionCluster.getLocalNodeString(), new byte[]{0});
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
