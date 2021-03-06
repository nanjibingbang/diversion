package com.diversion.transport.netty;

import com.alibaba.fastjson.JSON;
import com.diversion.node.DiversionCluster;
import com.diversion.node.DiversionNode;
import com.diversion.transport.packet.Packet;
import com.diversion.transport.Charset;
import com.diversion.transport.IoChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;

/**
 * 写超时事件处理（客户端）
 *
 * @author liou
 */
@Sharable
public class WriteIdleChannelHandler extends AbstractExceptionChannelHandler {

    public static final AttributeKey<byte[]> CTX_BEARTBEAT = AttributeKey.newInstance("CTX_BEARTBEAT");

    private Charset charset;

    public WriteIdleChannelHandler(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                // 写入一个心跳包
                ctx.writeAndFlush(getHeartbeat(ctx.channel()));
            }
        } else if (IoChannel.EVENT_INIT_COMPLETE.equals(evt)) {
            // channel连接后发送一个心跳包提供server端接入所需信息
            ctx.writeAndFlush(getHeartbeat(ctx.channel()));
        }
    }

    private ByteBuf getHeartbeat(Channel channel) {
        Attribute<byte[]> attr = channel.attr(CTX_BEARTBEAT);
        byte[] beartbeat = attr.get();
        if (beartbeat == null) {
            Map<String, Object> content = new HashMap<>();
            DiversionCluster diversionCluster = channel.attr(AttributeKey.<DiversionCluster>valueOf("cluster")).get();
            content.put("node", diversionCluster.getLocalNodeString());
            Packet packet = new Packet(JSON.toJSONString(content).getBytes(charset.charset()));
            packet.request().beartbeat().charsetCode(charset.code());
            beartbeat = packet.packing();
            attr.set(beartbeat);
        }
        ByteBuf buf = channel.alloc().ioBuffer(beartbeat.length);
        buf.writeBytes(beartbeat);
        return buf;
    }

    @Override
    public void handleException(Channel channel) {
        DiversionCluster cluster = channel.attr(AttributeKey.<DiversionCluster>valueOf("cluster")).get();
        DiversionNode node = channel.attr(AttributeKey.<DiversionNode>valueOf("node")).get();
        if (cluster != null && node != null) {
            cluster.nodeUnreachable(node, true);
        }
    }

}