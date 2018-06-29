package com.diversion.zk;

import java.util.List;

/**
 * Content : zookeeper子节点变更处理器
 *
 * @author liou 2017-12-29.
 */
public interface ChildrenChangeHandler {

    /**
     * 获取所监控的zookeeper节点路径
     *
     * @return
     */
    String getNamespace();

    void build(List<String> children) throws Exception;

    /**
     * 子节点添加
     *
     * @param child
     */
    void handleChildAdded(String child);

    /**
     * 子节点移除
     *
     * @param child
     */
    void handleChildRemoved(String child);

}
