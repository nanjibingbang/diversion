package com.liou.diversion.node;

import com.liou.diversion.utils.HashUtils;

/**
 * 虚拟节点，降低单点热度 actual指向代理节点
 */
public class VirtualNode implements HashNode {

    private final String key;

    private final int hash;

    private final DiversionNode actual;

    /**
     * @param key    用于hash计算
     * @param actual 为null时指定为this
     */
    public VirtualNode(String key, DiversionNode actual) {
        this.key = key;
        this.actual = actual == null ? (DiversionNode) this : actual;
        hash = HashUtils.hash(getKey());
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * 实际指向的节点
     *
     * @return
     */
    public DiversionNode getActual() {
        return actual;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof VirtualNode) {
            VirtualNode virtualNode = (VirtualNode) obj;
            return getKey().equals(virtualNode.getKey());
        }
        return false;
    }

}
