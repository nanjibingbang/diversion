package com.liou.diversion.transport.packet;

import com.liou.diversion.transport.Charset;
import com.liou.diversion.utils.ByteUtils;

/**
 * 应用层传输协议封装<br>
 * 
 * 首部2字节占用（3位版本标识，1位请求确认，1位响应完成，1位心跳包标识，1位是否有唯一标识，
 * 1位保留，5位编码标识同时标志为文本内容传输，3位负载长度字节标识）<br>
 * 负载长度（最长4字节）<br>
 * [唯一标识]负载 单包最大长度0x7fffffff
 *
 * @author liou
 *
 */
public class Packet {

    private int head = 0;

    private final byte[] payload;// 负载

    private byte[] lenBytes;// 长度字节

    public static final int MAX_PAYLOAD_LENGTH = Integer.MAX_VALUE;

    public Packet(byte[] bs) {
        int pl = (payload = bs) == null ? 0 : payload.length;
        if (MAX_PAYLOAD_LENGTH < pl) {
            throw new IllegalArgumentException("too much bytes");
        }
        lenBytes = ByteUtils.toBytes(pl, false);
        head = (byte) lenBytes.length;
    }

    public Packet(byte[] bs, int offset, int len) {
        if (bs == null || offset < 0 || len <= 0 || offset + len > bs.length) {
            throw new IllegalArgumentException();
        }
        if (MAX_PAYLOAD_LENGTH < len) {
            throw new IllegalArgumentException("too much bytes");
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

    public Packet setReq() {
        head |= 0x1000;
        return this;
    }

    public boolean isResp() {
        return is(0x800);
    }

    public Packet setResp() {
        head |= 0x800;
        return this;
    }

    public boolean isBeartbeat() {
        return is(0x400);
    }

    public Packet setBeartbeat() {
        head |= 0x400;
        return this;
    }

    public boolean hasUuid() {
        return is(0x200);
    }

    public Packet setHasUuid() {
        head |= 0x200;
        return this;
    }

    public int getUuid() {
        return ByteUtils.toNum(payload, 0, 4);
    }

    public boolean isText() {
        return getCharsetCode() != 0;
    }

    public int getCharsetCode() {
        return (head & 0xf8) >> 3;
    }

    public Packet setCharsetCode(int charsetCode) {
        head =(head & 0xff07) + ((charsetCode << 3) & 0xf8);
        return this;
    }

    /**
     * 获取实际内容 不包含唯一标识
     * @return
     */
    public byte[] getPayload() {
        boolean hasUuid = hasUuid();
        int offset = hasUuid ? 4 : 0;
        byte[] content = new byte[payload.length - offset];
        System.arraycopy(payload, offset, content, 0, content.length);
        return content;
    }

    public final byte[] packing() {
        int len = 2 + lenBytes.length + (payload == null ? 0 : payload.length);
        byte[] result = new byte[len];
        result[0] = (byte) ((head & 0xff00) >> 8);
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