package com.ss.main;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author JDsen99
 * @description 1
 * @createDate 2021/8/18-18:34
 */
public class TestDemo {
    public static void main(String[] args) {
        new TestDemo().testReplaceAll();
    }

    /**
     * 测试Properties
     */

     public void testProperties() throws IOException{
         Properties p = new Properties();
         p.load(new InputStreamReader(TestDemo.class.getClassLoader()
                 .getSystemResourceAsStream("application.properties"), "utf-8"));
         System.out.println(p.getProperty("test"));
     }

     /**
      * 测试replaceAll
      */

      public void testReplaceAll(){
          String url = "https://www.zhihu.com/search?q";

          String contextPath = "https://www.zhihu.com";

          url = url.replace(contextPath,"").replaceAll("/+","/");
          System.out.println(url);
      }
}
