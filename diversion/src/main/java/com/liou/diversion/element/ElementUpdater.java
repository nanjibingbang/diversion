package com.liou.diversion.element;

import com.liou.diversion.Adapter;

/**
 * 节点添加该接口实现以更新
 *
 * @author liou
 */
public interface ElementUpdater extends Adapter<Element> {

    Object update(Element element) throws Exception;

}
