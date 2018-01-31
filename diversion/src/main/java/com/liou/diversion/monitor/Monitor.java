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
        Map<String, String> clusterStatus = new HashMap<>();
        Iterator<DiversionNode> it = diversionCluster.getNodeSet().iterator();
        while (it.hasNext()) {
            DiversionNode diversionNode = it.next();
            clusterStatus.put(diversionNode.getKey(),
                    diversionCluster.isReady(diversionNode) ? "ready" : "not ready");
        }
        return JSON.toJSONString(clusterStatus);
    }

    public void removeNode(String sign) {
        DiversionNode node = diversionCluster.getNode(sign);
        diversionCluster.removeNode(node);
    }
    
    public void setDiversionCluster(DiversionCluster diversionCluster) {
        this.diversionCluster = diversionCluster;
    }

    public void shutdownCluster() {
        diversionCluster.destroy();
    }

}
