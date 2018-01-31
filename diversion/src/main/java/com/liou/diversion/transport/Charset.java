package com.liou.diversion.transport;

public enum Charset {

    UTF8(java.nio.charset.Charset.forName("UTF-8"), 1), GBK(java.nio.charset.Charset.forName("GBK"), 2), ISO88591(
            java.nio.charset.Charset.forName("ISO8859-1"), 3), UTF16(java.nio.charset.Charset.forName("UTF-16"), 4);

    private Charset(java.nio.charset.Charset charset, int code) {
        this.charset = charset;
        this.code = code;
    }

    private java.nio.charset.Charset charset;
    private int code;

    public java.nio.charset.Charset charset() {
        return charset;
    }

    public int code() {
        return code;
    }

    public static Charset fromCode(int code) {
        Charset[] values = Charset.values();
        for (Charset charset : values) {
            if (charset.code == code) {
                return charset;
            }
        }
        return null;
    }

    public static Charset fromName(String name) {
        java.nio.charset.Charset forName = java.nio.charset.Charset.forName(name);
        Charset[] values = Charset.values();
        for (Charset charset : values) {
            if (charset.charset.equals(forName)) {
                return charset;
            }
        }
        return null;
    }

}
