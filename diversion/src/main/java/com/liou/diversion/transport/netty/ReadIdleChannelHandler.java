package com.liou.diversion.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liou.diversion.node.DiversionCluster;
import com.liou.diversion.node.DiversionNode;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * channel读超时事件处理（服务端）
 * 
 * @author liou
 *
 */
@Sharable
public class ReadIdleChannelHandler extends ExceptionChannelHandler {

    private static Logger logger = LoggerFactory.getLogger(ReadIdleChannelHandler.class);

    /**
     * 超时后断开连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if ((evt instanceof IdleStateEvent) && (IdleState.READER_IDLE == ((IdleStateEvent) evt).state())) {
            Channel channel = ctx.channel();
            logger.warn("{} reader idle, close channel", channel);
            closeChannel(channel);
        }
    }

    private void closeChannel(Channel channel) {
        DiversionCluster cluster = channel.attr(AttributeKey.<DiversionCluster> valueOf("cluster")).get();
        DiversionNode node = channel.attr(AttributeKey.<DiversionNode> valueOf("node")).get();
        if (cluster != null && node != null) {
            cluster.nodeUnreachable(node, false);
        }
    }

    @Override
    public void exceptionClose(Channel channel) {
        closeChannel(channel);
    }

}