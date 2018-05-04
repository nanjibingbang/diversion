package com.liou.diversion.transport.netty;

import com.liou.diversion.element.Element;
import com.liou.diversion.element.DiversionService;
import com.liou.diversion.element.execute.ExecuteContext;
import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.utils.HessianUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
                nettyChannel.result(packet.uuid(), deserialized);
            } else {
                Element element = (Element) deserialized;
                ExecuteContext executeContext = new ExecuteContext(packet.uuid(), ctx.channel());
                diversionService.executeUpdate(executeContext, element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
