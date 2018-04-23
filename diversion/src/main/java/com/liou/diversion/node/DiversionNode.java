package com.liou.diversion.node;

import com.liou.diversion.element.Element;
import com.liou.diversion.transport.ChannelManager;
import com.liou.diversion.transport.IoChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点模型，一致性hash计算及channel管理
 */
public class DiversionNode extends VirtualNode implements HashNode, ChannelManager {

    private static Logger logger = LoggerFactory.getLogger(DiversionNode.class);

    private IoChannel channel;

    public DiversionNode(String nodeSign) {
        super(nodeSign, null);
    }

    @Override
    public boolean isReady() {
        return channel != null && channel.isActive();
    }

    @Override
    public void channel(IoChannel channel) {
        if (this.channel != null && this.channel != channel) {
            this.channel.close();
        }
        this.channel = channel;
        this.channel.addAttribute("node", this);
    }

    @Override
    public IoChannel channel() {
        return channel;
    }

    @Override
    public void closeChannel() {
        channel.close();
        channel = null;
    }

    /**
     * 从节点获取Element
     *
     * @param element
     * @return
     */
    public Object receiveRemote(Element element, long timeout) {
        return channel.request(element, timeout);
    }

    @Override
    public String toString() {
        return String.format("DiversionNode[%s]", getKey());
    }

}
