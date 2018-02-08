package com.liou.diversion.transport;

/**
 * Content : channel管理
 *
 * @author liou 2017-11-01.
 */
public interface ChannelManager {
    /**
     * 判断channel是否处于可通信状态
     *
     * @return
     */
    boolean isReady();

    /**
     * 强制更新channel并关闭之前的channel
     *
     * @param channel
     */
    void channel(IoChannel channel);

    /**
     * 获取channel
     *
     * @return
     */
    IoChannel channel();

    /**
     * 关闭channel
     */
    void closeChannel();
}
