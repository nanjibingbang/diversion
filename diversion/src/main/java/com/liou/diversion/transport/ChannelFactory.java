package com.liou.diversion.transport;

import java.io.IOException;
import java.net.SocketAddress;

import com.liou.diversion.container.Initialization;
import com.liou.diversion.node.AccessService;

public interface ChannelFactory extends Initialization {

    /**
     * 连接到节点
     * 
     * @param host
     * @param port
     * @param localAddress
     *            本地地址
     * @return 成功创建的IoChannel，或者null创建失败
     */
    IoChannel createChannel(String host, int port, SocketAddress localAddress) throws IOException;

    /**
     * channel重连
     * 
     * @param channel
     * @param localAddress
     *            本地地址
     * @return 成功创建的IoChannel，或者null创建失败
     * @throws IOException
     */
    IoChannel reconnect(IoChannel channel, SocketAddress localAddress) throws IOException;

    /**
     * accept port<br>
     * 阻塞过程
     * 
     * @param port
     * @param accessService
     * @throws Throwable
     */
    void acceptOn(int port, AccessService accessService) throws IOException, InterruptedException;

    void shutdown();

}