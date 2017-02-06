package com.danielkim.expensemanager.Utils;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Daniel on 2/5/2017.
 */

public class MyDateUtils {
    public static Date convertDateToNearestMonthYear(Date d){
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);

        return c.getTime();
    }

    public static String convertMonthIntToLongName(int month){
        String monthString;
        switch (month) {
            case 1:  monthString = "January";       break;
            case 2:  monthString = "February";      break;
            case 3:  monthString = "March";         break;
            case 4:  monthString = "April";         break;
            case 5:  monthString = "May";           break;
            case 6:  monthString = "June";          break;
            case 7:  monthString = "July";          break;
            case 8:  monthString = "August";        break;
            case 9:  monthString = "September";     break;
            case 10: monthString = "October";       break;
            case 11: monthString = "November";      break;
            case 12: monthString = "December";      break;
            default: monthString = "Invalid month"; break;
        }
        return monthString;
    }

    public static final String MONTH_YEAR_FORMAT_SQL = "%m %Y";

    // return month from Calendar formatted with month name (ex: January 2017)
    public static String getFormattedMonthYear(Calendar cal){
        String month = convertMonthIntToLongName(cal.get(Calendar.MONTH) + 1);
        int year = cal.get(Calendar.YEAR);
        return month + " " + year;
    }

    // monthYear: format = MONTH_YEAR_FORMAT_SQL (ex: 01 2017)
    // return month from string formatted with month name (ex: January 2017)
    public static String getFormattedMonthYear(String date){
        String month = convertMonthIntToLongName(Integer.parseInt(date.substring(0, 2)));
        String year = date.substring(3);

        return month + " " + year;
    }

    // monthYear: format = MONTH_YEAR_FORMAT_SQL (ex: 01 2017)
    public static Calendar convertMonthYearToCalendar(String date){
        int month = Integer.parseInt(date.substring(0, 2));
        int year = Integer.parseInt(date.substring(3));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        return cal;
    }

    // convert cal date to month year string
    public static String convertMonthYear(Calendar cal){
        int month = cal.get(Calendar.MONTH) + 1; // 0 indexed month
        int year = cal.get(Calendar.YEAR);
        return String.format("%02d", month) + " " + year;
    }

    // format date as numeric (ie: 01/27/2017, 1:00PM)
    public static String formatDateMillis(Context context, long dateMillis) {
        return DateUtils.formatDateTime(
                context,
                dateMillis,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
        );
    }

    // format date with full month name (ie: January 1, 2017, 1:00 PM)
    public static String formatDateMillisLong(Context context, long dateMillis) {
        return DateUtils.formatDateTime(
                context,
                dateMillis,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
        );
    }
}
