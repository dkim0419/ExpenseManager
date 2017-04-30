package com.danielkim.expensemanager.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Fragments.ChartsFragment;
import com.danielkim.expensemanager.Fragments.HistoryFragment;
import com.danielkim.expensemanager.Fragments.INavDrawerFragment;
import com.danielkim.expensemanager.Fragments.OverviewFragment;
import com.danielkim.expensemanager.MySharedPreferences;
import com.danielkim.expensemanager.R;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DBHelper database;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    // currency and budget
    private String mCurrency;
    private String mBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrency = MySharedPreferences.getCurrency(this);
        mBudget = MySharedPreferences.getBudget(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        database = new DBHelper(this);

        OverviewFragment fragment = new OverviewFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        setActionBarTitle(getResources().getString(R.string.nav_overview));
        navigationView.setCheckedItem(R.id.nav_overview);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        // Update Nav Drawer selection
                        updateNavDrawerSelection();
                    }
                });
    }

    private void updateNavDrawerSelection(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof INavDrawerFragment){
            // set nav drawer selection to current active fragment
            navigationView.setCheckedItem(((INavDrawerFragment) fragment).getNavDrawerId());
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavDrawerSelection();
        String currency = MySharedPreferences.getCurrency(this);
        String budget = MySharedPreferences.getBudget(this);

        if ((mCurrency == null || !mCurrency.equals(currency)) ||
                (mBudget == null || !mBudget.equals(budget))) {
            mCurrency = currency;
            mBudget = budget;

            Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }
    }

    public String getBudget() {
        return mBudget;
    }

    public String getCurrency() {
        return mCurrency;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, MyPreferenceActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());
        return true;
    }

    public void setActionBarTitle(String title){
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void displayView(int viewId){
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (viewId) {
            case R.id.nav_overview:
                fragment = new OverviewFragment();
                title  = getResources().getString(R.string.nav_overview);
                break;
            case R.id.nav_history:
                fragment = HistoryFragment.newInstance(Calendar.getInstance());
                break;
            case R.id.nav_charts:
                fragment = ChartsFragment.newInstance(Calendar.getInstance());
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        // set the toolbar title
        setActionBarTitle(title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
