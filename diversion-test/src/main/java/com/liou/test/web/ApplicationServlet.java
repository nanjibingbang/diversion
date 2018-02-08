package com.liou.test.web;

import com.alibaba.fastjson.JSON;
import com.liou.diversion.monitor.Monitor;
import com.liou.test.entity.Param;
import com.liou.test.entity.Result;
import com.liou.test.service.DataService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApplicationServlet extends HttpServlet {

    private static final long serialVersionUID = -4737630353471495998L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        if (requestURI.startsWith("/diversion-test/query")) {
            query(req, resp);
        } else if (requestURI.startsWith("/diversion-test/monitor")) {
            monitorInfo(req, resp);
        }
    }

    private void monitorInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String type = req.getParameter("type");
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext());
        Monitor monitor = webApplicationContext.getBean(Monitor.class);
        if ("info".equals(type)) {
            resp.getWriter().write(monitor.clusterInfo());
        } else if ("removeNode".equals(type)) {
            String nodeName = req.getParameter("nodeName");
            monitor.removeNode(nodeName);
        } else if ("shutdown".equals(type)) {
            monitor.shutdownCluster();
        } else if ("restartRedis".equals(type)) {
            restartJedis(webApplicationContext.getBean(Jedis.class));
        }
    }

    private void query(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext());
        DataService bean = webApplicationContext.getBean(DataService.class);
        Result data = null;
        try {
            Param param = new Param(req.getParameter("title"), Math.random(), 's', true);
            data = bean.getData(param);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        resp.getWriter().write(JSON.toJSONString(data));
    }

    private void restartJedis(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.disconnect();
            } catch (Exception e) {
            }
            jedis.connect();
        }
    }

}
