package com.liou.diversion.transport.netty;

import com.liou.diversion.element.Element;
import com.liou.diversion.transport.ChannelIoException;
import com.liou.diversion.transport.IoChannel;
import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.transport.request.RequestFuture;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class NettyChannel implements IoChannel {

    private static Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    /**
     * 对端地址
     */
    private final String host;

    /**
     * 对端端口
     */
    private final int port;

    private Channel channel;

    /**
     * 请求应答对应map
     */
//    private static ConcurrentSkipListMap<Integer, RequestFuture> waitMap = new ConcurrentSkipListMap<>();
    private static ConcurrentHashMap<Integer, RequestFuture> waitMap = new ConcurrentHashMap<>();

    public final static AttributeKey<NettyChannel> ATTRKEY_IOCHANNEL = AttributeKey.newInstance("ioChannelKey");

    protected NettyChannel(String host, int port, Channel channel) {
        this.host = host;
        this.port = port;
        setChannel(channel);
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    protected void setChannel(Channel channel) {
        channel.attr(ATTRKEY_IOCHANNEL).set(this);
        this.channel = channel;
    }

    protected Channel getChannel() {
        return channel;
    }

    @Override
    public void addAttribute(String key, Object obj) {
        Attribute<Object> attr = channel.attr(AttributeKey.valueOf(key));
        attr.set(obj);
    }

    @Override
    public void fireInited() {
        if (channel != null) {
            channel.pipeline().fireUserEventTriggered(EVENT_INIT_COMPLETE);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
        }
        channel = null;
    }

    // ------------------同步请求应答------------------

    @Override
    public Object request(Element element, long timeout) {
        int sign = 0;
        try {
            RequestFuture requestFuture = getExistRequest(element);
            if (requestFuture == null) {
                requestFuture = newRequestAndSend(element);
            }
            sign = requestFuture.getRequestSign();
            return requestFuture.syncGet(timeout);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            if (sign != 0) {
                waitMap.remove(sign);
            }
        }
    }

    /**
     * 获取已有的请求
     * @param element
     * @return
     */
    private RequestFuture getExistRequest(Element element) {
        try {
            Map<Integer, RequestFuture> map = waitMap;
            List<Map.Entry<Integer, RequestFuture>> entries = map.entrySet().stream()
                    .filter(entity -> entity.getValue().getElement().equals(element))
                    .collect(Collectors.toList());
            if (entries != null && entries.size() > 0) {
                return entries.get(0).getValue();
            }
        } catch (RuntimeException e) {
        }
        return null;
    }

    private RequestFuture newRequestAndSend(Element element) {
        RequestFuture requestFuture = new RequestFuture(element);
        int sign = requestFuture.getRequestSign();
        waitMap.put(sign, requestFuture);
        sendData(requestFuture.getRequestContent());
        return requestFuture;
    }

    public void result(int sign, Object response) {
        RequestFuture requestFuture = waitMap.get(sign);
        if (requestFuture != null) {
            requestFuture.setResponse(response);
        } else {
            // 超时抵达
            logger.warn("超时抵达：{}，{}", sign, response);
        }
    }

    @Override
    public void sendData(Packet data) {
        if (!isActive()) {
            throw new ChannelIoException();
        }
        channel.writeAndFlush(data);
    }

}
