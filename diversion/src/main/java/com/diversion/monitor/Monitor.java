package com.diversion.monitor;

import com.diversion.node.DiversionCluster;
import com.diversion.node.DiversionNode;

public class Monitor {

    private DiversionCluster diversionCluster;

    public String clusterInfo() {
        return diversionCluster.clusterInfo();
    }

    public void removeNode(String sign) {
        DiversionNode node = diversionCluster.getNode(sign);
        diversionCluster.nodeUnreachable(node, true);
    }

    public void setDiversionCluster(DiversionCluster diversionCluster) {
        this.diversionCluster = diversionCluster;
    }

    public void shutdownCluster() {
        diversionCluster.destroy();
    }

}
