package com.liou.diversion.transport.netty;

import com.liou.diversion.transport.packet.Packet;
import com.liou.diversion.transport.packet.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * Packet读写处理<br>
 *
 * @author liou
 */
@Sharable
public class PacketChannelHandler extends ChannelDuplexHandler {

    public static final AttributeKey<PacketBuffer> CTX_PACKET_BUF = AttributeKey.newInstance("CTX_PACKET_BUF");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            Attribute<PacketBuffer> pbAttr = ctx.channel().attr(CTX_PACKET_BUF);
            PacketBuffer packetBuffer = pbAttr.get();
            if (packetBuffer == null) {
                packetBuffer = new PacketBuffer();
                pbAttr.set(packetBuffer);
            }
            packetBuffer.append(byteBuf);
            Packet packet;
            while (null != (packet = packetBuffer.readPacket(true))) {
                ctx.fireChannelRead(packet);
            }
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            writePacket((Packet) msg, ctx, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void writePacket(Packet packet, ChannelHandlerContext ctx, ChannelPromise promise) {
        byte[] packing = packet.packing();
        ByteBuf buf = ctx.alloc().ioBuffer(packing.length);
        buf.writeBytes(packing);
        ctx.writeAndFlush(buf, promise);
    }

}