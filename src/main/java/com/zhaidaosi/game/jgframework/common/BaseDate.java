package com.zhaidaosi.game.jgframework.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseDate {

    public static String FORMAT_YY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_YY_MM_DD = "yyyy-MM-dd";

    /**
     * 转换时间to字符串
     * @param String format
     * @param long time
     * @return String
     */
    public static String time2String(String format, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(time);
        return formatter.format(curDate);
    }

    /**
     * 转换时间to字符串
     * @param String format
     * @return String
     */
    public static String time2String(String format) {
        return time2String(format, System.currentTimeMillis());
    }

    /**
     * 字符时间到long
     * @param date
     * @return
     */
    public static long string2Time(String date) {
        if (date != null && !date.equals("")) {
            return Timestamp.valueOf(date).getTime();
        }
        return 0l;
    }

}
