package com.ss.main.controller;

import com.ss.framework.annotation.Autowired;
import com.ss.framework.annotation.Controller;
import com.ss.framework.annotation.RequestMapping;
import com.ss.framework.annotation.RequestParam;
import com.ss.main.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/20-15:06
 */
@Controller("demoController")
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private DemoService demoService;

    @RequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name) throws Exception {
        String result =  demoService.getName(name);
//        demoService.doResponse(response);
//
//        String threadName = Thread.currentThread().getName();
//        Thread thread = Thread.currentThread();
//
//        System.out.println(threadName);
//        //response.getWriter().write(result);
    }

    @Override
    public String toString() {
        return "DemoController{" +
                "demoService=" + demoService +
                '}';
    }
}
