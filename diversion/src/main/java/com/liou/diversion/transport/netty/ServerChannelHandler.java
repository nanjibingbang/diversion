package com.liou.diversion.transport.netty;

import com.liou.diversion.element.Element;
import com.liou.diversion.element.execute.DiversionService;
import com.liou.diversion.node.DiversionNode;
import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.utils.HessianUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

@Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private DiversionService diversionService;

    public ServerChannelHandler(DiversionService diversionService) {
        this.diversionService = diversionService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Packet packet = (Packet) msg;
            Object deserialized = HessianUtils.deserialize(packet);
            if (packet.isResp()) {
                NettyChannel nettyChannel = ctx.channel().attr(NettyChannel.ATTRKEY_IOCHANNEL).get();
                nettyChannel.result(packet.getUuid(), deserialized);
            } else {
                Attribute<Object> nodeAttr = ctx.channel().attr(AttributeKey.valueOf("node"));
                DiversionNode requestNode = (DiversionNode) nodeAttr.get();
                Element element = (Element) deserialized;
                diversionService.executeUpdate(requestNode, packet.getUuid(), element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}