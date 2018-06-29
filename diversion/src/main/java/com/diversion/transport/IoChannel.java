package com.diversion.transport;

import com.diversion.element.Element;
import com.diversion.transport.request.RequestFuture;

import java.util.concurrent.TimeoutException;

/**
 * tcp i/o channel
 *
 * @author liou
 */
public interface IoChannel {

    public static final String EVENT_INIT_COMPLETE = "INIT COMPLETE";

    boolean isActive();

    /**
     * 执行同步请求 阻塞操作,io线程谨慎调用
     *
     * @param element 请求内容
     * @return 请求结果
     * @throws TimeoutException
     */
    Object request(Element element, long timeout);

    void sendRequest(RequestFuture request);

    void addAttribute(String key, Object obj);

    void fireInited();

    void close();

}
