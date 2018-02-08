package com.liou.diversion.node;

/**
 * hash节点
 *
 * @author liou 2017-11-01.
 */
public interface HashNode {

    /**
     * 节点key值
     *
     * @return
     */
    String getKey();

    /**
     * 计算hash值
     *
     * @return
     */
    int hashCode();

}
