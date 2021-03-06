package com.diversion.transport.packet;

import com.diversion.utils.HessianUtils;
import com.diversion.transport.Charset;

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
            return String.format("BEARTBEAT %s", new String(packet.payload(), Charset.fromCode(packet.charsetCode()).charset()));
        } else if (packet.isReq()) {
            return String.format("REQUEST sign:%s content:%s", packet.uuid(), HessianUtils.deserialize(packet));
        } else if (packet.isResp()) {
            return String.format("RESPONSE sign:%s content:%s", packet.uuid(), HessianUtils.deserialize(packet));
        } else {
            return "UNKNOWN";
        }
    }

}
