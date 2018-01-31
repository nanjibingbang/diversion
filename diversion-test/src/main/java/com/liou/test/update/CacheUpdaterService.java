package com.liou.test.update;

import com.liou.test.entity.Param;
import com.liou.test.entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 更新所有缓存
 *
 * @author liou
 */
@Component
public class CacheUpdaterService {

    public Result updateCache(Param param) {
        Result target = new Result();
        BeanUtils.copyProperties(param, target);
        return target;
    }

}
