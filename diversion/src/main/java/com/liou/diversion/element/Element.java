package com.liou.diversion.element;

import com.alibaba.fastjson.JSON;
import com.liou.diversion.utils.HashUtils;

import java.io.Serializable;

/**
 * @author liou
 */
public class Element implements Serializable {

    /**
     * 标识
     */
    private String signature;
    /**
     * 更新相关参数
     */
    private Object[] params;
    /**
     * 目标调用类
     */
    private String tagCla;
    /**
     * 目标方法
     */
    private String tagMed;

    private int hash;

    public Element(String tagCla, String tagMed, Object[] params) {
        this.tagCla = tagCla;
        this.tagMed = tagMed;
        this.params = params;
        hash = initHash();
    }

    protected int initHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(tagCla).append('#').append(tagMed).append('?');
        if (params != null) {
            for (Object object : params) {
                sb.append(JSON.toJSONString(object)).append("&");
            }
        }
        this.signature = sb.substring(0, sb.length() - 1);
        return HashUtils.hash(signature);
    }

    public String getSignature() {
        return signature;
    }

    public Object[] getParams() {
        return params;
    }

    public String getTagCla() {
        return tagCla;
    }

    public String getTagMed() {
        return tagMed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof Element) {
            Element other = (Element) obj;
            return signature.equals(other.signature);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return signature;
    }
}
