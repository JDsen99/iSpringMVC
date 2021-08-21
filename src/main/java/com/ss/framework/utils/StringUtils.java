package com.ss.framework.utils;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/20-15:42
 */
public class StringUtils {

    /**
     *  将首字母小写
     * @return
     */
    public static String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars).trim();
    }
}
