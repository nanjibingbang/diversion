package com.liou.diversion.container.spring;

import com.liou.diversion.container.ConfigApplyException;
import com.liou.diversion.container.DiversionConfig;
import com.liou.diversion.utils.ConfigUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author liou
 */
public class DiversionBeanPostProcessor implements BeanPostProcessor {

    private DiversionConfig diversionConfig;

    /**
     * 初始化属性
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            return ConfigUtils.applyConfig(bean, diversionConfig);
        } catch (IllegalAccessException e) {
            throw new ConfigApplyException("", e);
        }
    }

    /**
     * ElementUpdater bean收集
     *
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
