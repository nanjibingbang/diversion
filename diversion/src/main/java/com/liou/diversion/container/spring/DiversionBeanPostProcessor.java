package com.liou.diversion.container.spring;

import com.liou.diversion.container.Config;
import com.liou.diversion.container.DiversionConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.liou.diversion.element.ElementUpdaterRepository;
import com.liou.diversion.element.ElementUpdater;

import java.lang.reflect.Field;

/**
 *
 * @author liou
 *
 */
public class DiversionBeanPostProcessor implements BeanPostProcessor {

    private DiversionConfig diversionConfig;

    /**
     * 初始化属性
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            Config annotation = field.getAnnotation(Config.class);
            if(annotation != null) {
                String config = annotation.value();
                Object value = diversionConfig.getConfig(config);
                try {
                    field.setAccessible(true);
                    field.set(bean, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    /**
     * ElementUpdater bean收集
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        if (bean instanceof ElementUpdater) {
//            ElementUpdater updater = (ElementUpdater) bean;
//            ElementUpdaterRepository.registeElementUpdater(updater);
//        }
        return bean;
    }

    public void setDiversionConfig(DiversionConfig diversionConfig) {
        this.diversionConfig = diversionConfig;
    }
}
