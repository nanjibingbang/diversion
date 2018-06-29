package com.diversion.transport.netty;

import com.diversion.element.DiversionService;
import com.diversion.element.Element;
import com.diversion.element.execute.ExecuteContext;
import com.diversion.transport.packet.Packet;
import com.diversion.utils.HessianUtils;
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
            Object deserialize = HessianUtils.deserialize(packet);
            if (packet.isResp()) {
                NettyChannel nettyChannel = ctx.channel().attr(NettyChannel.ATTRKEY_IOCHANNEL).get();
                nettyChannel.result(packet.uuid(), deserialize);
            } else {
                Element element = (Element) deserialize;
                ExecuteContext executeContext = new ExecuteContext(packet.uuid(), ctx.channel());
                diversionService.executeUpdate(executeContext, element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
