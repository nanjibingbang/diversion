package com.liou.test.service;

import com.alibaba.fastjson.JSON;
import com.liou.diversion.element.aop.Diversion;
import com.liou.test.entity.Param;
import com.liou.test.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DataServiceImpl implements DataService {

    @Autowired
    private JedisPool jedisPool;

    @Diversion(targetClass = "cacheUpdaterService", targetMethod = "updateCache")
    @Override
    public Result getData(Param type) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String result = jedis.get(JSON.toJSONString(type));
            return result == null ? null : JSON.parseObject(result, Result.class);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
