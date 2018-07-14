package util;

import java.text.SimpleDateFormat;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class TimeUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获得指定时间戳对应的可读时间字符串
     * @param time
     * @param format
     * @return
     */
    public static String getTimeStr(long time, String format){
        if(null == format || format.trim().equals("")){
            format = DATE_FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(time);
    }

    public static String getTimeStr(long time){
        return getTimeStr(time, DATE_FORMAT);
    }

}
