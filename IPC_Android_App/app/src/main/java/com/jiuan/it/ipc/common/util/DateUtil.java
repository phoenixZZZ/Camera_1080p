package com.jiuan.it.ipc.common.util;

import android.text.TextUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static String getNowDateTime() throws ParseException {
        return getNowDateTime("/");
    }

    public static String getNowDateTime(String dateSeparator) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + dateSeparator + "MM" + dateSeparator + "dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        Date monday = c.getTime();

        return sdf.format(monday);
    }

    public static String getNow7DateTimeBefore() throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);
        Date monday = c.getTime();
        String preMonday = sdf.format(monday);

        return preMonday;
    }

    public static String convertToDateTime(Long ts) {

        Date date = new Date(ts);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(date);
    }

    public static String convertToTime(Long ts) {

        Date date = new Date(ts);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return sdf.format(date);
    }

    public static Long convertToTimestamp(String strTS) {

        Timestamp ts = Timestamp.valueOf(strTS);

        return ts.getTime();
    }

    public static Boolean isSameDay(String strDay1, String strDay2) {

        if (TextUtils.isEmpty(strDay1) || TextUtils.isEmpty(strDay2)) {
            return false;
        }

        return TextUtils.equals(strDay1.substring(0, 10), strDay2.substring(0, 10));
    }

}
