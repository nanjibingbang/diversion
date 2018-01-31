package com.liou.diversion.utils;

import com.liou.diversion.transport.Charset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 字符串hash计算<br>
 * <p>
 * murmurhash实现；更改自jedis MurmurHash32位实现
 */
public class HashUtils {

    public static int hash(String str) {
        byte[] bytes = str.getBytes(Charset.UTF8.charset());
        return murmur(bytes);
    }

    public static int murmur(byte[] bs) {
        ByteBuffer buf = ByteBuffer.wrap(bs);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        int seed = 0x1234ABCD;
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ buf.remaining();

        int k;
        while (buf.remaining() >= 4) {
            k = buf.getInt();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h *= m;
            h ^= k;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getInt();
            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;
        return h;
    }

}
