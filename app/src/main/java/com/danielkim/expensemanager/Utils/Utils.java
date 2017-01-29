package com.danielkim.expensemanager.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.inputmethod.InputMethodManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Daniel on 2/16/2016.
 * Collection of various utils
 */
public final class Utils {
    private Utils() {
    }

    // close soft keyboard
    public static void hideKeyboard(Activity activity){
        InputMethodManager mgr = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
    }

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

    // date should be in mm YYYY format
    public static String getFormattedMonthYear(String date){
        String month = convertMonthIntToLongName(Integer.parseInt(date.substring(0, 2)));
        String year = date.substring(3);

        return month + " " + year;
    }

    private static DecimalFormat twoDecimalPlaceFormat = new DecimalFormat("0.00");
    public static String formatDoubleTwoDecimalPlaces(double d){
        return twoDecimalPlaceFormat.format(d);
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

    public static int darkenColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }
}
