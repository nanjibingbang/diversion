package com.liou.test.controller;

import com.liou.test.entity.Result;
import com.liou.test.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 该key只缓存guid
     */
    private final static String KEY_OPERATION_POS_USEFUL = "CMS:operationPosUseful_%s_%d_%s";
    /**
     * 缓存guid对应运营位详细信息
     */
    private final static String KEY_OPERATION_PO_GUID = "CMS:operationPosGuid_%s";
    private final static String SCRIPT_OPERATION_PO_USEFUL = "local usefulKey='%s';\n" +
            "local guids=redis.call('lrange', usefulKey, 0, -1);\n" +
            "local results={};\n" +
            "for index=1, #guids do\n" +
            " local operation=redis.call('get', string.format('CMS:operationPosGuid_%s', guids[index]));\n" +
            " if (operation==nil or string.len(operation)==0)\n" +
            " then\n" +
            "  return {};\n" +
            " else\n" +
            "  results[index]=operation;\n" +
            " end\n" +
            "end\n" +
            "return results;";
    private static Logger logger = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private DataService dataService;
    @Autowired
    private JedisPool jedisPool;

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    public Result result(@RequestParam("title") String title) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String script = String.format(SCRIPT_OPERATION_PO_USEFUL, String.format(KEY_OPERATION_POS_USEFUL, 's', 1, 'a'), "%s");
            System.out.println(script);
            Object result = jedis.eval(script);
            System.out.println(result);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
