/**
 * 日期工具类，提供日期转换功能。
 * 该类主要用于将字符串日期转换为Date对象。
 */
package com.jianbing.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提供将日期字符串转换为Date对象的功能。
 * 使用的日期格式为"yyyy-MM-dd"。
 */
public class DateUtil {

    /**
     * 将日期字符串转换为Date对象。
     *
     * @param date 需要转换的日期字符串，应符合"yyyy-MM-dd"格式。
     * @return 转换后的Date对象。
     *         如果转换失败，将抛出RuntimeException。
     */
    public static Date get(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 尝试将传入的日期字符串转换为Date对象。
            return sdf.parse(date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
