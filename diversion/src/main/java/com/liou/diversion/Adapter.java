package com.liou.diversion;

/**
 * 适配器接口
 *
 * @author liou
 */
public interface Adapter<T> {

    boolean adapter(T element);

}