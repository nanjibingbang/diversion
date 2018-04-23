package com.liou.test.controller;

import com.liou.test.entity.Param;
import com.liou.test.entity.Result;
import com.liou.test.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private DataService dataService;

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    public Result result(@RequestParam("title") String title) {
        Result data = null;
        try {
            Param param = new Param(title, Math.random(), 's', true);
            data = dataService.getData(param);
        } catch (RuntimeException e) {
            logger.error("", e);
        }
        return data;
    }

}
