package com.ss.framework.annotation;

import java.lang.annotation.*;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/20-15:02
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
