package com.liou.diversion.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.liou.diversion.node.DiversionCluster;
import com.liou.diversion.node.DiversionNode;

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
