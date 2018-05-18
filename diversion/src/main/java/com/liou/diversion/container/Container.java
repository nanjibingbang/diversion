package com.liou.diversion.container;

import com.liou.diversion.container.spring.GetElementUpdaterException;
import com.liou.diversion.element.Element;
import com.liou.diversion.element.ElementUpdater;

/**
 * Content :
 *
 * @author liou 2018-01-12.
 */
public interface Container {

    /**
     * 根据名称从容器中获取对象
     *
     * @param name
     * @return
     */
    <T> T getInstance(String name);

    /**
     * 创建实例
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T newInstance(Class<T> clazz) throws Exception;

    /**
     * 根据element相关信息从容器获取Updater
     *
     * @param element
     * @return
     */
    ElementUpdater getElementUpdater(Element element) throws GetElementUpdaterException;
}
