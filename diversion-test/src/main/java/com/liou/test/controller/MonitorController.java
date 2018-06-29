package com.liou.test.controller;

import com.diversion.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private Monitor monitor;

    @RequestMapping("/info")
    public String info() {
        return monitor.clusterInfo();
    }

    @RequestMapping(value = "/node", method = RequestMethod.DELETE)
    public String removeNode(@RequestParam("nodeName") String name) {
        monitor.removeNode(name);
        return monitor.clusterInfo();
    }

}
