package com.liou.diversion.utils;

/**
 * @author liou
 */
public class ByteUtils {

    public static final int BYTE_BLANK = 0x7fffffff;

    public static int toNum(byte[] bs, int offset, int len) {
        int result = 0;
        int end = offset + len;
        for (int i = end; i > offset; i--) {
            result |= Byte.toUnsignedInt(bs[i - 1]) << (Byte.SIZE * (end - i));
        }
        return result;
    }

    /**
     * 
     * @param num
     * @param fix4Byte 是否固定4字节
     * @return
     */
    public static byte[] toBytes(int num, boolean fix4Byte) {
        byte[] bs = new byte[Integer.BYTES];
        int index = Integer.BYTES - 1;
        while (num != 0) {
            bs[index--] = (byte) (num & 0xff);
            num = num >>> Byte.SIZE;
        }
        if (fix4Byte) {
            return bs;
        }
        byte[] result = new byte[Integer.BYTES - index - 1];
        System.arraycopy(bs, index + 1, result, 0, result.length);
        return result;
    }

    public static int toOpposite(int i) {
        if (i >= 0) {
            return i | 0x80000000;
        } else {
            return i & 0x7fffffff;
        }
    }

}