package com.liou.diversion.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;

/**
 * Content :
 *
 * @author liou 2017-12-29.
 */
public class ZookeeperListener implements CuratorListener {

    @Override
    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
    }
}
