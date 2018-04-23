package com.liou.test.configuration;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author : liou
 * @Date : 2018/4/18
 * @Description : rediscluster configuration
 */
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfiguration {

    private Integer port;

    private String host;

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool() {
        if (port == null || StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("port, host");
        }
        JedisPoolConfig config = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(config, host, port);
        return jedisPool;
    }

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
}
