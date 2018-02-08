package com.liou.diversion.element.execute;

import com.liou.diversion.element.Element;
import com.liou.diversion.element.ElementUpdater;
import com.liou.diversion.element.cache.TransientProvider;
import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.utils.HessianUtils;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 更新{@link Element}任务
 *
 * @author liou
 */
public class ElementUpdateTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ElementUpdateTask.class);

    private final Element element;
    private final List<ExecuteContext> contexts;

    private Object result;
    private Exception cause;
    private volatile boolean done;

    private TransientProvider transientProvider;
    private ElementUpdater elementUpdater;

    public ElementUpdateTask(Element element, ElementUpdater elementUpdater, TransientProvider transientProvider) {
        done = false;
        contexts = new ArrayList<>();
        this.element = element;
        this.transientProvider = transientProvider;
        this.elementUpdater = elementUpdater;
    }

    protected boolean addContext(ExecuteContext executeContext) {
        synchronized (contexts) {
            if (isDone()) {
                return false;
            }
            if (executeContext != null && !contexts.contains(executeContext)) {
                contexts.add(executeContext);
            }
        }
        return true;
    }

    @Override
    public void run() {
        try {
            result = elementUpdater.update(element);
            if (result == null) {
                result = "";
            }
            transientProvider.record(result, element);
        } catch (Exception e) {
            cause = e;
            result = null;
        } finally {
            done = true;
            synchronized (contexts) {
                contexts.forEach(executeContext -> {
                    Packet packet = HessianUtils.serialize(result, executeContext.sign).setResp();
                    executeContext.channel.writeAndFlush(packet);
                });
            }
            // 唤醒本地同步获取
            synchronized (this) {
                notifyAll();
            }
            logger.debug("update element {}", element, cause);
        }
    }

    /**
     * 任务是否执行完成
     *
     * @return
     */
    public boolean isDone() {
        return done;
    }

    /**
     * 获取执行结果
     *
     * @return
     */
    public synchronized Object get(long timeout) throws TimeoutException {
        if (!isDone()) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(String.format("sync request error %s", element), e);
            }
        }
        if (!isDone()) {
            throw new TimeoutException(String.format("更新:%s 超时", element));
        }
        return result;
    }

    /**
     * 异常结果
     *
     * @return
     */
    public Exception cause() {
        return cause;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof ElementUpdateTask) {
            ElementUpdateTask other = (ElementUpdateTask) obj;
            return this.element.equals(other.element);
        }
        return false;
    }

}
