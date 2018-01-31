package com.liou.test;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import redis.clients.jedis.Jedis;

public class JedisFactory implements FactoryBean<Jedis>, InitializingBean {

    private Jedis jedis;

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
    public Jedis getObject() throws Exception {
        return jedis;
    }

    @Override
    public Class<?> getObjectType() {
        return Jedis.class;
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
        jedis = new Jedis(host, port);
    }

}
