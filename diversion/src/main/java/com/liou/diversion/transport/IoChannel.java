package com.liou.diversion.transport;

import java.util.concurrent.TimeoutException;

import com.liou.diversion.element.Element;
import com.liou.diversion.transport.packet.Packet;

/**
 * tcp i/o channel
 * 
 * @author liou
 *
 */
public interface IoChannel {

    public static final String EVENT_INIT_COMPLETE = "INIT COMPLETE";

    boolean isActive();

    /**
     * 执行同步请求 阻塞操作,io线程谨慎调用
     * 
     * @param element
     *            请求内容
     * @return 请求结果
     * @throws TimeoutException
     */
    Object request(Element element, long timeout);

    void sendData(Packet data);

    void addAttribute(String key, Object obj);

    void fireInited();
    
    void close();

}
