package com.liou.test;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolFactory implements FactoryBean<JedisPool>, InitializingBean {

    private JedisPool jedisPool;

    private Integer port;

    private String host;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public JedisPool getObject() throws Exception {
        return jedisPool;
    }

    @Override
    public Class<?> getObjectType() {
        return JedisPool.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (port == null || StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("port, host");
        }
        JedisPoolConfig config = new JedisPoolConfig();
        jedisPool = new JedisPool(config, host, port);
    }

}
