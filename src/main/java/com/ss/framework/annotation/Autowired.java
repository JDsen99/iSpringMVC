package com.ss.framework.annotation;

import java.lang.annotation.*;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/20-15:00
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
