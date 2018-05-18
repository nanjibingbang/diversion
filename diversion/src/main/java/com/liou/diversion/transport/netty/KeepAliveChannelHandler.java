package com.liou.diversion.transport.netty;

import com.liou.diversion.node.DiversionCluster;
import com.liou.diversion.node.DiversionNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端保活处理
 *
 * @author liou
 */
@Sharable
public class KeepAliveChannelHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(KeepAliveChannelHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        DiversionCluster cluster = channel.attr(AttributeKey.<DiversionCluster>valueOf("cluster")).get();
        DiversionNode node = channel.attr(AttributeKey.<DiversionNode>valueOf("node")).get();
        /*
         * 不在节点集中的或主动关闭的不重连
         */
        if (cluster != null && node != null && cluster.isRegistered(node)) {
            logger.info("inactive channel {}, reconnecting...", channel);
            try {
                cluster.nodeReconnect(node);
                logger.info("reconnect {} success", channel.remoteAddress());
            } catch (Exception e) {
                logger.error("reconnet failer", e);
                cluster.nodeUnreachable(node, false);
            }
        }
        ctx.fireChannelInactive();
    }

}
