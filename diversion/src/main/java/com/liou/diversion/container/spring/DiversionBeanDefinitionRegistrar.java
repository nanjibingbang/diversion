package com.liou.diversion.container.spring;

import com.liou.diversion.EnableDiversion;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.container.Initialization;
import com.liou.diversion.element.aop.DiversionInterceptor;
import com.liou.diversion.element.cache.DefaultTransientProvider;
import com.liou.diversion.element.DiversionService;
import com.liou.diversion.element.execute.ElementTaskExecutor;
import com.liou.diversion.monitor.Monitor;
import com.liou.diversion.node.DiversionCluster;
import com.liou.diversion.transport.netty.NettyChannelFactory;
import com.liou.diversion.zk.ZookeeperClient;
import com.liou.diversion.zk.ZookeeperRegister;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * diversion整合spring，开启AspectJ Auto Proxy
 *
 * @author liou
 */
public class DiversionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    class KeyValue {
        public KeyValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        String key;
        Object value;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 开启aspectj代理
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

        // 切点注册
        doRegistry(registry, DiversionInterceptor.class, null,
                new KeyValue[]{new KeyValue("diversionService", "diversionService")});
        //
        doRegistry(registry, SpringContainer.class, null, null);

        // 配置解析
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableDiversion.class.getName()));
        String locations = annotationAttributes.getString("locations");
        String fileEncoding = annotationAttributes.getString("fileEncoding");
        String beanName = DiversionPropertiesSupport.class.getName();
        if (registry.isBeanNameInUse(beanName)) {
            throw new IllegalArgumentException(beanName + "已经注册");
        }
        BeanDefinitionBuilder genericBeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(DiversionPropertiesSupport.class);
        genericBeanDefinition.addPropertyValue("configPath", locations);
        if (StringUtils.isNotBlank(fileEncoding)) {
            genericBeanDefinition.addPropertyValue("fileEncoding", fileEncoding);
        }
        AbstractBeanDefinition beanDefinition = genericBeanDefinition.getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
        // bean处理
        doRegistry(registry, DiversionBeanPostProcessor.class, null,
                new KeyValue[]{new KeyValue("diversionConfig", DiversionPropertiesSupport.class.getName())});
        // 暂存器
        doRegistry(registry, DefaultTransientProvider.class, "transientProvider", null);
        // 执行线程池
        doRegistry(registry, ElementTaskExecutor.class, "elementTaskExecutor",
                new KeyValue[]{new KeyValue("transientProvider", "transientProvider"),
                        new KeyValue("container", SpringContainer.class.getName())});
        // diversion service
        doRegistry(registry, DiversionService.class, "diversionService",
                new KeyValue[]{new KeyValue("transientProvider", "transientProvider"),
                        new KeyValue("elementTaskExecutor", "elementTaskExecutor"),
                        new KeyValue("diversionCluster", "diversionCluster")});
        // netty
        doRegistry(registry, NettyChannelFactory.class, "channelFactory",
                new KeyValue[]{new KeyValue("diversionService", "diversionService")});
        // 节点集
        doRegistry(registry, DiversionCluster.class, "diversionCluster",
                new KeyValue[]{new KeyValue("channelFactory", "channelFactory")});
        // monitor
        doRegistry(registry, Monitor.class, null,
                new KeyValue[]{new KeyValue("diversionCluster", "diversionCluster")});
        // zookeeper
        doRegistry(registry, ZookeeperClient.class, "zookeeperClient", null);
        doRegistry(registry, ZookeeperRegister.class, null, new KeyValue[]{
                new KeyValue("diversionCluster", "diversionCluster"),
                new KeyValue("zookeeperClient", "zookeeperClient")});
    }

    /**
     * @param registry
     * @param beanClazz
     * @param beanName
     * @param references
     * @return
     */
    private String doRegistry(BeanDefinitionRegistry registry, Class<?> beanClazz, String beanName, KeyValue[] references) {
        if (beanName == null) {
            beanName = beanClazz.getName();
        }
        if (registry.isBeanNameInUse(beanName)) {
            throw new IllegalArgumentException(beanName + "已经注册");
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
        if (references != null) {
            for (int i = 0; i < references.length; i++) {
                KeyValue reference = references[i];
                String value = reference.value.toString();
                beanDefinitionBuilder.addPropertyReference(reference.key, value);
            }
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        if (Destroyable.class.isAssignableFrom(beanClazz)) {
            beanDefinition.setDestroyMethodName("destroy");
        }
        if (Initialization.class.isAssignableFrom(beanClazz)) {
            beanDefinition.setInitMethodName("init");
        }
        registry.registerBeanDefinition(beanName, beanDefinition);
        return beanName;
    }

}
