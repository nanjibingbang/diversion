package com.liou.diversion.container.spring;

import com.liou.diversion.container.Container;
import com.liou.diversion.container.DiversionConfig;
import com.liou.diversion.element.Element;
import com.liou.diversion.element.ElementUpdater;
import com.liou.diversion.element.ElementUpdaterProxy;
import com.liou.diversion.utils.ConfigUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Content :
 *
 * @author liou 2018-01-12.
 */
public class SpringContainer implements Container, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(String name) {
        return (T) applicationContext.getBean(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> clazz) throws Exception {
        DiversionConfig diversionConfig = applicationContext.getBean(DiversionConfig.class);
        return (T) ConfigUtils.applyConfig(clazz.newInstance(), diversionConfig);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public ElementUpdater getElementUpdater(Element element) throws GetElementUpdaterException {
        List<Class<?>> clazzs = new ArrayList<>();
        Object[] params = element.getParams();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                clazzs.add(params[i].getClass());
            }
        }
        Class[] medTypes = clazzs.toArray(new Class[clazzs.size()]);
        try {
            Object object = getInstance(element.getTagCla());
            return new ElementUpdaterProxy(object, element.getTagCla(),
                    element.getTagMed(), medTypes);
        } catch (Exception e) {
            throw new GetElementUpdaterException(e, element);
        }
    }
}
