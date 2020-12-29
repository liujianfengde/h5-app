package cn.liuxiaoer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String formatDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return simpleDateFormat.format(calendar.getTime());
    }
}
