package com.danielkim.expensemanager.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.danielkim.expensemanager.Activities.ViewCategoryPMActivity;
import com.danielkim.expensemanager.BuildConfig;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.MySharedPreferences;

/**
 * Created by Daniel on 4/25/2017.
 */

public class PreferencesFragment extends PreferenceFragment {
    private PreferenceScreen mViewCategoriesPref;
    private PreferenceScreen mViewPaymentMethodsPref;
    private EditTextPreference mSetBudgetPref;
    private EditTextPreference mSetCurrencyPref;
    private Preference mAboutPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mViewCategoriesPref = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_add_category_title));
        mViewCategoriesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ViewCategoryPMActivity.class);
                i.putExtra(ViewCategoryPMActivity.VIEW_TYPE, ViewCategoryPMActivity.ViewType.Category);
                startActivity(i);
                return true;
            }
        });

        mViewPaymentMethodsPref = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_add_payment_method_title));
        mViewPaymentMethodsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ViewCategoryPMActivity.class);
                i.putExtra(ViewCategoryPMActivity.VIEW_TYPE, ViewCategoryPMActivity.ViewType.PaymentMethod);
                startActivity(i);
                return true;
            }
        });

        mSetBudgetPref = (EditTextPreference) findPreference(getResources().getString(R.string.pref_budget_title));
        mSetBudgetPref.setSummary(getResources().getString(R.string.pref_budget_desc, MySharedPreferences.getCurrency(getActivity()), MySharedPreferences.getBudget(getActivity())));
        mSetBudgetPref.setText(MySharedPreferences.getBudget(getActivity()));
        mSetBudgetPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!TextUtils.isEmpty(newValue.toString().trim())) {
                    Long newBudget = Long.parseLong(newValue.toString());
                    MySharedPreferences.updateBudget(getActivity(), newBudget);
                    mSetBudgetPref.setSummary(getResources().getString(R.string.pref_budget_desc, MySharedPreferences.getCurrency(getActivity()), MySharedPreferences.getBudget(getActivity())));
                }
                return true;
            }
        });

        mSetCurrencyPref = (EditTextPreference) findPreference(getResources().getString(R.string.pref_currency_title));
        mSetCurrencyPref.setSummary(getResources().getString(R.string.pref_currency_desc, MySharedPreferences.getCurrency(getActivity())));
        mSetCurrencyPref.setText(MySharedPreferences.getCurrency(getActivity()));
        mSetCurrencyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String currency = newValue.toString().trim();
                if (!TextUtils.isEmpty(currency)) {
                    MySharedPreferences.updateCurrency(getActivity(), currency);
                    mSetCurrencyPref.setSummary(getResources().getString(R.string.pref_currency_desc, MySharedPreferences.getCurrency(getActivity())));
                    mSetBudgetPref.setSummary(getResources().getString(R.string.pref_budget_desc, MySharedPreferences.getCurrency(getActivity()), MySharedPreferences.getBudget(getActivity())));
                }
                return true;
            }
        });

        mAboutPref = findPreference(getResources().getString(R.string.pref_about_title));
        mAboutPref.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));
        mAboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LayoutInflater dialogInflater = getActivity().getLayoutInflater();
                View openSourceLicensesView = dialogInflater.inflate(R.layout.fragment_about, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(openSourceLicensesView)
                        .setTitle((getString(R.string.pref_about_title)))
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
                return true;
            }
        });
    }
}
