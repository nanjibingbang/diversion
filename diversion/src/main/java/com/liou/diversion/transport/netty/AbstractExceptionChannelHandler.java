package com.liou.diversion.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExceptionChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ReadIdleChannelHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        logger.error("channel:{}关闭", channel);
        handleException(channel);
    }

    /**
     * 异常关闭channel
     *
     * @param channel
     */
    public abstract void handleException(Channel channel);

}