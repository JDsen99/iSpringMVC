package com.ss.main.service;

import com.ss.framework.annotation.Autowired;
import com.ss.framework.annotation.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/20-15:07
 */
@Service("demoService")
public class DemoService {

    private static Map<Integer,HttpServletResponse> container = new HashMap<>();

    private static int idx = 1;

    public String getName(String name) {
        return name;
    }

//    public void doResponse(HttpServletResponse response) throws Exception{
////        response.getWriter().write("111111111111111111111111111111");
//        container.put(idx++,response);
//        System.out.println(container.size());
//        System.out.println(response);
//        doRequest();
//    }
//
//    private void doRequest() throws IOException {
//    private void doRequest() throws IOException {
//    }
}
