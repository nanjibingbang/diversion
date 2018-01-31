package com.liou.diversion.node;

/**
 * 接入控制服务
 * 
 * @author liou
 *
 */
public interface AccessService {

    /**
     * 节点接入
     * 
     * @param diversionNode
     * @return
     */
    boolean access(DiversionNode diversionNode);

}