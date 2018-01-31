package com.liou.diversion.element.cache;

import com.liou.diversion.element.Element;

/**
 * 暂存器
 * @author liou
 *
 */
public interface TransientProvider {

    /**
     * 获取暂存数据
     * @param element
     * @return
     */
    TransientResult get(Element element);

    /**
     * 记录暂存数据
     * @param result
     * @param element
     */
    void record(Object result, Element element);
}
