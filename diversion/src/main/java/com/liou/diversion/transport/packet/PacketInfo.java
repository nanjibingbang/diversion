package com.liou.diversion.transport.packet;

import com.liou.diversion.transport.Charset;
import com.liou.diversion.utils.HessianUtils;

/**
 * Content :
 *
 * @author liou 2018-01-26.
 */
public class PacketInfo {

    private Packet packet;

    public PacketInfo(Packet packet) {
        this.packet = packet;
    }

    @Override
    public String toString() {
        if (packet.isBeartbeat()) {
            return String.format("BEARTBEAT %s", new String(packet.getPayload(), Charset.fromCode(packet.getCharsetCode()).charset()));
        } else if (packet.isReq()) {
            return String.format("REQUEST sign:%s content:%s", packet.getUuid(), HessianUtils.deserialize(packet));
        } else if (packet.isResp()) {
            return String.format("RESPONSE sign:%s content:%s", packet.getUuid(), HessianUtils.deserialize(packet));
        } else {
            return "UNKNOWN";
        }
    }

}
