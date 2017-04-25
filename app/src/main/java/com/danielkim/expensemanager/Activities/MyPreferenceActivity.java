package com.danielkim.expensemanager.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.danielkim.expensemanager.R;

/**
 * Created by Daniel on 3/30/2017.
 */

public class MyPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference viewCategoriesPref = findPreference(getResources().getString(R.string.pref_add_category_title));
        viewCategoriesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getApplicationContext(), ViewCategoryPMActivity.class);
                i.putExtra(ViewCategoryPMActivity.VIEW_TYPE, ViewCategoryPMActivity.ViewType.Category);
                startActivity(i);
                return true;
            }
        });
    }
}
