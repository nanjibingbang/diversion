package com.diversion.transport.request;

import com.diversion.element.Element;
import com.diversion.transport.packet.Packet;
import com.diversion.utils.HessianUtils;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestFuture {

    // 节点请求标识自增序列号
    private static AtomicInteger generated;

    static {
        generated = new AtomicInteger(1);
    }

    private final Element element;
    private final Packet requestContent;
    private final int requestSign;
    // future是否完成
    private volatile boolean done;
    private Object response;

    public RequestFuture(Element element) {
        done = false;
        this.element = element;
        this.requestSign = generated.getAndIncrement();
        requestContent = HessianUtils.serialize(element, requestSign);
        requestContent.request();
    }

    public static int currentRequestSign() {
        return generated.get();
    }

    public Element getElement() {
        return element;
    }

    /**
     * 请求
     *
     * @return
     */
    public Packet getRequestContent() {
        return requestContent;
    }

    public int getRequestSign() {
        return requestSign;
    }

    public boolean isDone() {
        return done;
    }

    public Object get() {
        return response;
    }

    public synchronized Object syncGet(long timeout) throws TimeoutException {
        if (!isDone()) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                done = true;
                throw new RuntimeException(String.format("sync request error %s", element), e);
            }
        }
        if (!isDone()) {
            throw new TimeoutException(String.format("sync request timeout %s", element));
        }
        return response;
    }

    public synchronized void setResponse(Object response) {
        this.response = response;
        done = true;
        notifyAll();
    }

}
