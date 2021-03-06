package com.diversion.transport.packet;

import com.diversion.utils.ByteUtils;

/**
 * 应用层传输协议封装<br>
 * <p>
 * 首部2字节占用（3位版本标识，1位请求确认，1位响应完成，1位心跳包标识，1位是否有唯一标识，
 * 1位保留，5位编码标识同时标志为文本内容传输，3位负载长度字节标识）<br>
 * 负载长度（最长4字节）<br>
 * [唯一标识]负载 单包最大长度0x7fffffff
 *
 * @author liou
 */
public class Packet {

    public static final long MAX_PAYLOAD_LENGTH = 0x7fffffffffffffL - 1;
    public static final int BEARTBEAT_SIGN = 0x400;
    private final byte[] payload;// 负载
    private int head = 0;
    private byte[] lenBytes;// 长度字节

    public Packet(byte[] bs) {
        int pl = (payload = bs) == null ? 0 : payload.length;
        lenBytes = ByteUtils.toBytes(pl, false);
        head = (byte) lenBytes.length;
    }

    public Packet(byte[] bs, int offset, int len) {
        if (bs == null || offset < 0 || len <= 0 || offset + len > bs.length) {
            throw new IllegalArgumentException();
        }
        payload = new byte[len];
        System.arraycopy(bs, offset, payload, 0, len);
        lenBytes = ByteUtils.toBytes(len, false);
        head = (byte) lenBytes.length;
    }

    protected Packet(int head, byte[] payload) {
        this.head = head;
        this.payload = payload;
    }

    public boolean is(int sign) {
        return (head & sign) == sign;
    }

    public int getVersion() {
        return head >> 13;
    }

    public Packet setVersion(int version) {
        head = (head & 0x1fff) + (version & 0x7 << 13);
        return this;
    }

    public boolean isReq() {
        return is(0x1000);
    }

    public Packet request() {
        head |= 0x1000;
        return this;
    }

    public boolean isResp() {
        return is(0x800);
    }

    public Packet response() {
        head |= 0x800;
        return this;
    }

    public boolean isBeartbeat() {
        return is(BEARTBEAT_SIGN);
    }

    public Packet beartbeat() {
        head |= 0x400;
        return this;
    }

    public boolean hasUuid() {
        return is(0x200);
    }

    public int uuid() {
        return ByteUtils.toNum(payload, 0, 4);
    }

    public Packet withUuid() {
        head |= 0x200;
        return this;
    }

    public boolean isText() {
        return charsetCode() != 0;
    }

    public int charsetCode() {
        return (head & 0xf8) >> 3;
    }

    public Packet charsetCode(int charsetCode) {
        head = (head & 0xff07) + ((charsetCode << 3) & 0xf8);
        return this;
    }

    /**
     * 获取实际内容 不包含唯一标识
     *
     * @return
     */
    public byte[] payload() {
        boolean hasUuid = hasUuid();
        int offset = hasUuid ? 4 : 0;
        byte[] content = new byte[payload.length - offset];
        System.arraycopy(payload, offset, content, 0, content.length);
        return content;
    }

    public final byte[] packing() {
        int len = 2 + lenBytes.length + (payload == null ? 0 : payload.length);
        byte[] result = new byte[len];
        result[0] = (byte) (head >> 8);
        result[1] = (byte) (head & 0xff);
        int destPos = 2;
        System.arraycopy(lenBytes, 0, result, destPos, lenBytes.length);
        destPos += lenBytes.length;
        if (payload != null) {
            System.arraycopy(payload, 0, result, destPos, payload.length);
        }
        return result;
    }

}