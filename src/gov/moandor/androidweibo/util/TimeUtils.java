package gov.moandor.androidweibo.util;

import android.content.res.Resources;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtils {
    private static final DateFormat sSinaFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    private static final DateFormat sDayFormat12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
    private static final DateFormat sDayFormat24 = new SimpleDateFormat("H:mm", Locale.ENGLISH);
    private static final DateFormat sDateFormat12 = new SimpleDateFormat("M-d h:mm a", Locale.ENGLISH);
    private static final DateFormat sDateFormat24 = new SimpleDateFormat("M-d H:mm", Locale.ENGLISH);
    private static final DateFormat sYearFormat12 = new SimpleDateFormat("yyyy-M-d h:mm a", Locale.ENGLISH);
    private static final DateFormat sYearFormat24 = new SimpleDateFormat("yyyy-M-d H:mm", Locale.ENGLISH);
    
    public static String getListTime(AbsItemBean bean) {
        try {
            long timeMillis = parseSinaTime(bean);
            return getListTime(timeMillis);
        } catch (ParseException e) {
            Logger.logExcpetion(e);
            return "";
        }
    }
    
    private static String getListTime(long msg) {
        Resources res = GlobalContext.getInstance().getResources();
        long now = System.currentTimeMillis();
        long sec = (now - msg) / 1000;
        if (sec < 60) {
            return res.getString(R.string.just_now);
        }
        long min = sec / 60;
        if (min < 60) {
            return res.getString(R.string.min_ago, min);
        }
        long hrs = min / 60;
        Calendar nowCalendar = Calendar.getInstance();
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTimeInMillis(msg);
        if (hrs < 24 && nowCalendar.get(Calendar.DAY_OF_YEAR) == msgCalendar.get(Calendar.DAY_OF_YEAR)) {
            if (android.text.format.DateFormat.is24HourFormat(GlobalContext.getInstance())) {
                return res.getString(R.string.today) + " " + sDayFormat24.format(msg);
            } else {
                return res.getString(R.string.today) + " " + sDayFormat12.format(msg);
            }
        }
        long days = hrs / 24;
        if (days < 30 && nowCalendar.get(Calendar.DAY_OF_YEAR) - msgCalendar.get(Calendar.DAY_OF_YEAR) == 1) {
            if (android.text.format.DateFormat.is24HourFormat(GlobalContext.getInstance())) {
                return res.getString(R.string.yesterday) + " " + sDayFormat24.format(msg);
            } else {
                return res.getString(R.string.yesterday) + " " + sDayFormat12.format(msg);
            }
        }
        if (nowCalendar.get(Calendar.YEAR) == msgCalendar.get(Calendar.YEAR)) {
            if (android.text.format.DateFormat.is24HourFormat(GlobalContext.getInstance())) {
                return sDateFormat24.format(msg);
            } else {
                return sDateFormat12.format(msg);
            }
        }
        if (android.text.format.DateFormat.is24HourFormat(GlobalContext.getInstance())) {
            return sYearFormat24.format(msg);
        } else {
            return sYearFormat12.format(msg);
        }
    }
    
    public static long parseSinaTime(AbsItemBean bean) throws ParseException {
        return sSinaFormat.parse(bean.createdAt).getTime();
    }
}
