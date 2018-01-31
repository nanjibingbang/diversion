package com.liou.diversion.element.execute;

import com.liou.diversion.container.Config;
import com.liou.diversion.element.Element;
import com.liou.diversion.element.cache.TransientProvider;
import com.liou.diversion.element.cache.TransientResult;
import com.liou.diversion.node.DiversionCluster;
import com.liou.diversion.node.DiversionNode;
import com.liou.diversion.transport.ChannelIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * diversion请求处理
 *
 * @author liou
 */
public class DiversionService {
    private static Logger logger = LoggerFactory.getLogger(DiversionService.class);

    private DiversionCluster diversionCluster;
    private TransientProvider transientProvider;
    private ElementTaskExecutor elementTaskExecutor;

    @Config("diversion.request.timeout")
    private long requestTimeout;

    /**
     * 从cluster获取Element更新结果<br>
     * <p>
     *
     * @param element
     * @return
     */
    public Object receiveElement(Element element) throws Exception {
        long begin = System.currentTimeMillis();
        try {
            // 优先从暂存获取
            TransientResult transientResult = transientProvider.get(element);
            if (transientResult != null) {
                return transientResult.getResult();
            }
            DiversionNode selected = diversionCluster.select(element.hashCode());
            logger.debug("select node {}", selected);
            Object result = null;
            if (diversionCluster.isLocalNode(selected)) {
                result = localUpdate(element);
            } else {
                try {
                    result = selected.receiveRemote(element, requestTimeout);
                } catch (ChannelIoException e) {
                    // 节点存在IO问题时在本地执行更新
                    logger.warn("Unreachable Node:{}, Use Local", selected, e);
                    result = localUpdate(element);
                }
            }
            // 记录到本地暂存器
            transientProvider.record(result, element);
            return result;
        } finally {
            logger.debug("time spend:{}ms", System.currentTimeMillis() - begin);
        }
    }

    /**
     * 本地更新
     *
     * @param element
     * @return
     */
    public Object localUpdate(Element element) throws Exception {
        // 进入队列执行 同步获取结果
        ElementUpdateTask task = elementTaskExecutor.execute(element, null);
        Object result = task.get(requestTimeout);
        if (task.cause() != null) {
            throw task.cause();
        }
        return result;
    }

    /**
     * Element更新请求
     *
     * @param diversionNode 请求节点
     * @param sign          请求标识
     * @param element       请求内容
     * @return
     */
    public void executeUpdate(DiversionNode diversionNode, int sign, Element element) {
        Object result = transientProvider.get(element);
        if (result != null) {
            diversionNode.responseUpdated(result, sign);
        } else {
            ExecuteContext executeContext = new ExecuteContext(sign, diversionNode);
            try {
                elementTaskExecutor.execute(element, executeContext);
            } catch (Exception e) {
            }
        }
    }

    public void setDiversionCluster(DiversionCluster diversionCluster) {
        this.diversionCluster = diversionCluster;
    }

    public void setTransientProvider(TransientProvider transientProvider) {
        this.transientProvider = transientProvider;
    }

    public void setElementTaskExecutor(ElementTaskExecutor elementTaskExecutor) {
        this.elementTaskExecutor = elementTaskExecutor;
    }

}
