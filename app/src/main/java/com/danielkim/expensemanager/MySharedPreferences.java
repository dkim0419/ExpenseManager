package com.danielkim.expensemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Daniel on 4/29/2017.
 */

public class MySharedPreferences {
    private static String PREF_CURRENCY = "pref_currency";
    private static String PREF_BUDGET = "pref_budget";

    private static long DEFAULT_BUDGET = 1000;
    private static String DEFAULT_CURRENCY = "$";

    public static void updateBudget(Context context, long budget) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(PREF_BUDGET, budget);
        editor.apply();
    }

    public static String getBudget(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Long.toString(preferences.getLong(PREF_BUDGET, DEFAULT_BUDGET));
    }

    public static void updateCurrency(Context context, String currency) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_CURRENCY, currency);
        editor.apply();
    }

    public static String getCurrency(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_CURRENCY, DEFAULT_CURRENCY);
    }
}
