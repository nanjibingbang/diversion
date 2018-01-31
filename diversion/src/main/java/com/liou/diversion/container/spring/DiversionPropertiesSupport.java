package com.liou.diversion.container.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ResourceUtils;

import com.liou.diversion.container.DiversionConfig;
import com.liou.diversion.container.DiversionConfig.Configs;
import com.liou.diversion.transport.Charset;

/**
 * 
 * spring容器中 diversion配置处理
 * 
 * @author liou
 *
 */
public class DiversionPropertiesSupport extends PropertiesLoaderSupport
        implements BeanFactoryPostProcessor, FactoryBean<DiversionConfig> {

    private DiversionConfig diversionConfig;
    private String configPath;

    public DiversionPropertiesSupport() {
        diversionConfig = DiversionConfig.INSTANCE;
    }

    @Override
    public DiversionConfig getObject() throws Exception {
        return diversionConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return DiversionConfig.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected Properties mergeProperties() throws IOException {
        Properties properties = super.mergeProperties();
        recordConfig(properties);
        return properties;
    }

    private void parseLocation() {
        FileSystemResourceLoader fileSystemResourceLoader = new FileSystemResourceLoader();
        DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
        List<Resource> resources = new ArrayList<>();
        if (StringUtils.isNotBlank(configPath)) {
            String[] locations = configPath.split(";");
            for (String ls : locations) {
                Resource resource = null;
                if (ls.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                    resource = defaultResourceLoader.getResource(ls);
                } else {
                    resource = fileSystemResourceLoader.getResource(ls);
                }
                resources.add(resource);
            }
        }
        setLocations(resources.toArray(new Resource[resources.size()]));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            parseLocation();
            mergeProperties();
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not load properties", ex);
        }
    }

    private void recordConfig(Properties properties) {
        Configs[] configs = Configs.values();
        for(Configs config : configs) {
            String sign = config.sign();
            String property = properties.getProperty(sign);
            if(StringUtils.isNotBlank(property)) {
                diversionConfig.addConfig(config, convert(property, config.clazz()));
            }
        }
    }

    private Object convert(String property, Class<?> clazz) {
        if(clazz == Integer.class) {
            return Integer.valueOf(property);
        } else if(clazz == Charset.class) {
            Charset charset = Charset.fromName(property);
            if(charset == null) {
                charset = Charset.UTF8;
            }
            return charset;
        } else {
            return property;
        }
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

}
