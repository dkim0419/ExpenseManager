package com.danielkim.expensemanager.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danielkim.expensemanager.Activities.ViewCategoryPMActivity;
import com.danielkim.expensemanager.R;

/**
 * Created by Daniel on 4/25/2017.
 */

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference viewCategoriesPref = findPreference(getResources().getString(R.string.pref_add_category_title));
        viewCategoriesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ViewCategoryPMActivity.class);
                i.putExtra(ViewCategoryPMActivity.VIEW_TYPE, ViewCategoryPMActivity.ViewType.Category);
                startActivity(i);
                return true;
            }
        });

        Preference viewPaymentMethods = findPreference(getResources().getString(R.string.pref_add_payment_method_title));
        viewPaymentMethods.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ViewCategoryPMActivity.class);
                i.putExtra(ViewCategoryPMActivity.VIEW_TYPE, ViewCategoryPMActivity.ViewType.PaymentMethod);
                startActivity(i);
                return true;
            }
        });
    }
}
