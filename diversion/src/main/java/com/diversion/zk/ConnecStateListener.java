package com.diversion.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * Content :
 *
 * @author liou 2017-12-29.
 */
public class ConnecStateListener implements ConnectionStateListener {

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
    }

}
