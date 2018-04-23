package com.liou.diversion.transport.packet;

import com.liou.diversion.utils.ByteUtils;
import io.netty.buffer.ByteBuf;

/**
 * 
 * 
 * 2.0 抛弃已读包数据 ensuresize避免不必要的内存重新分配<br>
 * 2.1 修改ensuresize对添加大比特数组与连续添加比特数组 新分配内存长度计算策略不同<br>
 * 
 * 非线程安全<br>
 * 
 * @author liou
 *
 */
public class PacketBuffer {

    private byte[] bytes;
    private int size;
    private int ps;// next packet start

    private int head;
    private byte[] payload;

    private int capacity;

    public PacketBuffer() {
        this(1);
    }

    public PacketBuffer(int capacity) {
        if (capacity > 0) {
            this.capacity = capacity;
        }
        bytes = new byte[this.capacity];
    }

    /**
     * flow to next packet
     * 
     * @return <code>true</code> 如果buffer中含有至少一个packet,否则返回 <code>false</code>
     */
    private boolean next() {
        if (size > ps + 1) {
            head = (bytes[ps] << 8) + bytes[ps + 1];
            int pl = 2; // packet length
            int lenlen = head & 0x7;
            if (size >= ps + pl + lenlen) {
                int payLoadLen = ByteUtils.toNum(bytes, ps + pl, lenlen);
                pl += lenlen;
                if (size >= ps + pl + payLoadLen) {
                    payload = new byte[payLoadLen];
                    System.arraycopy(bytes, ps + pl, payload, 0, payLoadLen);
                    ps += pl + payLoadLen;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 适应ByteBuf
     * 
     * @param byteBuf
     * @return
     */
    public PacketBuffer append(ByteBuf byteBuf) {
        int readableBytes;
        if (byteBuf == null || (readableBytes = byteBuf.readableBytes()) == 0) {
            return this;
        }
        ensuresize(readableBytes);
        byteBuf.readBytes(bytes, size, readableBytes);
        size += readableBytes;
        return this;
    }

    public PacketBuffer append(byte[] bs) {
        if (bs == null) {
            throw new IllegalArgumentException();
        }
        return append(bs, 0, bs.length);
    }

    public PacketBuffer append(byte[] bs, int offset, int len) {
        if (bs == null || offset < 0 || offset + len > bs.length) {
            throw new IllegalArgumentException();
        }
        ensuresize(len);
        System.arraycopy(bs, offset, bytes, size, len);
        size += len;
        return this;
    }

    private void ensuresize(int len) {
        if (bytes.length - size < len) {
            int lpe = ps;// last packet end
            size = size - lpe;
            int newLen = ps == 0 ? (size + len) << 1 : size + (len << 1);
            ps = 0;
            byte[] newBs;
            if (bytes.length < newLen) {
                newBs = new byte[newLen];
            } else {
                newBs = bytes;
            }
            System.arraycopy(bytes, lpe, newBs, 0, size);
            bytes = newBs;
        }
    }

    public Packet readPacket(boolean skipBeartBeat) {
        boolean next;
        for(;(next = next()) && skipBeartBeat && (head & Packet.BEARTBEAT_SIGN) == Packet.BEARTBEAT_SIGN;) {
        }
        if (next) {
            return new Packet(head, payload);
        }
        return null;
    }

    // public void resize() {
    // System.arraycopy(bytes, ps, bytes, 0, size);
    // ps = 0;
    // }

    public void clean() {
        bytes = new byte[capacity];
        size = 0;
        ps = 0;
        System.gc();
    }

}