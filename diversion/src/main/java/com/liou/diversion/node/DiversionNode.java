package com.liou.diversion.node;

import com.liou.diversion.element.Element;
import com.liou.diversion.transport.ChannelIoException;
import com.liou.diversion.transport.IoChannel;
import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.utils.HessianUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点模型，一致性hash计算及channel管理
 */
public class DiversionNode extends VirtualNode implements HashNode {

    private static Logger logger = LoggerFactory.getLogger(DiversionNode.class);

    private final String host;
    private final int port;
    private IoChannel channel;

    public DiversionNode(String host, int port) {
        super(String.format("%s->%s", host, port), null);
        this.host = host;
        this.port = port;
    }

    public boolean isReady() {
        return channel != null && channel.isActive();
    }

    /**
     * 强制更新channel并关闭之前的channel
     */
    public void channel(IoChannel channel) {
        if (this.channel != null && this.channel != channel) {
            this.channel.close();
        }
        this.channel = channel;
        this.channel.addAttribute("node", this);
    }

    public IoChannel channel() {
        return channel;
    }

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

    /**
     * 回传更新结果
     *
     * @param result
     * @param sign
     */
    public void responseUpdated(Object result, int sign) {
        try {
            Packet serialized = HessianUtils.serialize(result, sign);
            serialized.setResp();
            channel.sendData(serialized);
        } catch (ChannelIoException e) {
            logger.warn("Unreachable Node:{}", this, e);
        } catch (RuntimeException e) {
            logger.error("response error:{},{}", result, sign, e);
        }
    }

    @Override
    public String toString() {
        return String.format("DiversionNode [host=%s, port=%s]", host, port);
    }

}
