package com.ss.framework;

import com.ss.framework.annotation.*;
import com.ss.framework.utils.StringUtils;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/18-17:38
 */
public class DispatchServlet extends HttpServlet {

    /**
     * 属性配置文件
     */
    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList();

    private ConcurrentHashMap<String,Object> iocContainer = new ConcurrentHashMap<>();

    private HashMap<String,Method> handlerMapping = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doDispatch(req,resp);

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、初始化参数
        doLoadConfig(config.getInitParameter("ConfigLocation"));

        //2、扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化所有的相关的类的实例，并保存到IOC容器中。
        doInstance();

        //4、依赖注入
        doAutowired();

        //5、构建HandlerMapping
        initHandlerMapping();

        //


    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(is);
        }catch (Exception e) {
            System.out.println("ERROR -- 1 读取配置文件失败");
        }finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String scanPath) {

        //判断
        if ("".equals(scanPath) || scanPath == null) {
            System.out.println("ERROR --  scanPackage 路径不存在");
            return;
        }

        URL url = this.getClass().getClassLoader().getResource(scanPath.replace(".", "/"));

        File dir = new File(url.getFile());

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    doScanner(scanPath + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    classNames.add(scanPath +"."+ file.getName().replace(".class", "").trim());
                }
            }
        }
    }

    private void doInstance() {
        //特判 若无class 则终止
        if (classNames.isEmpty()) return;

        try {

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller annotation = clazz.getAnnotation(Controller.class);
                String controllerName = annotation.value();
                //获取clazz Name
                if ("".equals(controllerName) || controllerName == null) {
                   controllerName = StringUtils.lowerFirstCase(clazz.getName());
                }
                iocContainer.put(controllerName,clazz.newInstance());
            }else if (clazz.isAnnotationPresent(Service.class)) {
                Service annotation = clazz.getAnnotation(Service.class);
                String controllerName = annotation.value();
                //获取clazz Name
                if ("".equals(controllerName) || controllerName == null) {
                    controllerName = StringUtils.lowerFirstCase(clazz.getName());
                }
                iocContainer.put(controllerName,clazz.newInstance());
            }

        }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void doAutowired() {
        if (iocContainer.isEmpty()) return;

        Set<Map.Entry<String, Object>> entries = iocContainer.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) continue;

                Autowired autowired = field.getAnnotation(Autowired.class);
                String value = autowired.value().trim();

                //System.out.println("-------" + field.getName());

                if ("".equals(value) || value == null) {
                    //value = StringUtils.lowerFirstCase(field.getName());
                    value = field.getName();
                }
                //TODO
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),iocContainer.get(value));
                } catch (IllegalAccessException e) {
                    System.out.println("ERROR -- "+entry.getValue().getClass().getName()+"的 "+ field.getName() + " 属性注入异常");
                    e.printStackTrace();
                    continue;
                }

            }
        }

    }

    private void initHandlerMapping() {
        if (iocContainer.isEmpty()) return;
        Set<Map.Entry<String, Object>> entries = iocContainer.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(Controller.class)) continue;

            String baseUrl = "";

            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();

            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) continue;

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String value = requestMapping.value();

                String url = (baseUrl + value.replaceAll("/+","/"));
                handlerMapping.put(url,method);
             //   System.out.println("INFO -- 请求路径为" + url);
            }
        }
    }


    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (handlerMapping.isEmpty()) return;

        String url = request.getRequestURI();

        String contextPath = request.getContextPath();

        url = url.replace(contextPath,"").replaceAll("/+","/");

        if (!this.handlerMapping.containsKey(url)) {
            response.getWriter().write("404 Not Found!!");
            return;
        }

        // 从handlerMapping中获取方法类
        Method method = handlerMapping.get(url);
        Class<?>[] methodParameterTypes = method.getParameterTypes();

        //从request中获取参数
        Map<String, String[]> requestParams = request.getParameterMap();

        Object[] paramValues = new Object[methodParameterTypes.length];

        for (int i = 0; i < methodParameterTypes.length; i++) {
            Class methodParameterType = methodParameterTypes[i];
            if (methodParameterType == HttpServletRequest.class) {
                paramValues[i] = request;
                continue;
            } else if (methodParameterType == HttpServletResponse.class) {
                paramValues[i] = response;
                continue;
            } else {

                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                String paramValue = "";
                for (Annotation[] parameterAnnotation : parameterAnnotations) {
                    for (Annotation annotation : parameterAnnotation) {
                        if (annotation instanceof RequestParam) {
                            paramValue = ((RequestParam) annotation).value();
                        }
                    }
                }
                //if (methodParameterType.isAnnotationPresent(RequestParam.class))

                Set<Map.Entry<String, String[]>> entries = requestParams.entrySet();
                for (Map.Entry<String, String[]> entry : entries) {
//                    String value = Arrays.toString(entry.getValue())
//                            .replaceAll("\\[|\\]","")
//                            .replaceAll(",\\s",",");
//                    paramValues[i] = value;

//                    System.out.println(entry.getKey() + "----");
//                    String[] value = entry.getValue();
//                    for (String s : value) {
//                        System.out.println(s);
//                    }
//                    paramValue
                    String[] value = requestParams.get(paramValue);
                    paramValues[i] = value[0];
                }
            }

            String beanName = method.getDeclaringClass().getSimpleName();
            try {
//                System.out.println(beanName);
//                for (Object paramValue : paramValues) {
//                    System.out.println(paramValue);
//                }
//
//                Object o = iocContainer.get(beanName);
//                System.out.println(o);

                beanName = StringUtils.lowerFirstCase(beanName);
                method.invoke(iocContainer.get(beanName),paramValues);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
