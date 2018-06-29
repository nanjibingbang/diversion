package com.diversion.element;

import com.diversion.container.Config;
import com.diversion.element.execute.ElementTaskExecutor;
import com.diversion.element.execute.ElementUpdateTask;
import com.diversion.element.execute.ExecuteContext;
import com.diversion.element.cache.TransientProvider;
import com.diversion.element.cache.TransientResult;
import com.diversion.node.DiversionCluster;
import com.diversion.node.DiversionNode;
import com.diversion.transport.ChannelIoException;
import com.diversion.transport.packet.Packet;
import com.diversion.utils.HessianUtils;
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

    @Config("request.timeout")
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
            return doReceiveElement(element);
        } finally {
            long spend = System.currentTimeMillis() - begin;
            if (spend > 50) {
                logger.error("time spend:{}ms", spend);
            }
            logger.debug("time spend:{}ms", spend);
        }
    }

    private Object doReceiveElement(Element element) throws Exception {
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
                // 节点存在IO问题, 断开节点连接并重试
                logger.warn("Unreachable Node:{}, retrying", selected, e);
                diversionCluster.nodeUnreachable(selected, true);
                result = doReceiveElement(element);
            }
        }
        // 记录到本地暂存器
        transientProvider.record(result, element);
        return result;
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
     * @param executeContext 请求上下文
     * @param element        请求内容
     * @return
     */
    public void executeUpdate(ExecuteContext executeContext, Element element) {
        Object result = transientProvider.get(element);
        if (result != null) {
            Packet packet = HessianUtils.serialize(result, executeContext.sign).response();
            executeContext.channel.writeAndFlush(packet);
        } else {
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
