package com.liou.test.update;

import com.alibaba.fastjson.JSON;
import com.liou.test.entity.Param;
import com.liou.test.entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 更新所有缓存
 *
 * @author liou
 */
@Component
public class CacheUpdaterService {

    @Autowired
    private JedisPool jedisPool;

    public Result updateCache(Param param) {
        Result target = new Result();
        BeanUtils.copyProperties(param, target);
        Jedis resource = jedisPool.getResource();
        try {
            resource.set(JSON.toJSONString(param), JSON.toJSONString(target));
        } finally {
            resource.close();
        }
        return target;
    }

}
