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

    private static DecimalFormat twoDecimalPlaceFormat = new DecimalFormat("0.00");
    public static String formatDoubleTwoDecimalPlaces(double d){
        return twoDecimalPlaceFormat.format(d);
    }

    public static int darkenColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }
}
