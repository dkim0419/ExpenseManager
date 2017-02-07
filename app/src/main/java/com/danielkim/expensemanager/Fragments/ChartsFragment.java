package com.danielkim.expensemanager.Fragments;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 1/29/2017.
 */

public class ChartsFragment extends Fragment implements NavDrawerFragment, LoaderManager.LoaderCallbacks<Cursor> {
    private DBHelper db;
    private static final int LOADER_ID = 0;
    private static final String ARGS_CUR_DISPLAYED_DATE = "mCurDisplayedDate";
    private static final String[] PROJECTION =
        new String[]
                {
                        "strftime('" + MyDateUtils.MONTH_YEAR_FORMAT_SQL + "', t1." + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch')",
                        "t1." + DBHelper.ExpensesTable.COL_AMOUNT,
                        "t2." + DBHelper.CategoriesTable.COL_CATEGORY,
                        "t1." + DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID,
                };

    private static final String SELECTION = "strftime('" + MyDateUtils.MONTH_YEAR_FORMAT_SQL + "', t1." + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch') = ?";
    private String[] selectionArgs;
    LoaderManager.LoaderCallbacks mCallbacks;

    private PieChart mPieChart;
    private ImageButton mNextMonthBtn;
    private ImageButton mPrevMonthBtn;
    private TextView mDate;

    private Calendar mCurDisplayedDate; // month-year of displayed chart data
    private Calendar mTodayDate; // month year of current date

    private HashMap<ExpenseCategory, Float> expensesByCategory;
    private HashMap<String, ExpenseCategory> mapCategoryNameToCategory;

    public static ChartsFragment newInstance(Calendar cal) {
        Bundle args = new Bundle();
        ChartsFragment fragment = new ChartsFragment();
        args.putSerializable(ARGS_CUR_DISPLAYED_DATE, cal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getNavDrawerId() {
        return R.id.nav_charts;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(getContext());
        Cursor c = db.getCategories();
        expensesByCategory = new HashMap<>();
        mapCategoryNameToCategory = new HashMap<>();
        //categoryColors = new ArrayList<>();

        // set month year to display
        Bundle args = getArguments();
        mCurDisplayedDate = (Calendar) args.getSerializable(ARGS_CUR_DISPLAYED_DATE);
        mTodayDate = mCurDisplayedDate;
        selectionArgs = new String[] {MyDateUtils.convertMonthYear(mCurDisplayedDate)};
        setActionBarTitle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_charts, container, false);
        mCallbacks = this;

        FrameLayout layout = (FrameLayout) v.findViewById(R.id.charts_container);
        mPieChart = new PieChart(getContext());
        mPieChart.setNoDataText(getResources().getString(R.string.chart_no_data));
        mPieChart.setNoDataTextColor(Color.WHITE);
        mPieChart.getLegend().setEnabled(false);
        mPieChart.setDescription(null);
        mPieChart.setEntryLabelColor(Color.WHITE);
        mPieChart.setTransparentCircleColor(Color.LTGRAY);

        layout.addView(mPieChart);

        mDate = (TextView) v.findViewById(R.id.txt_charts_date);
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurDisplayedDate = mTodayDate; // reset display month to today
                notifyDateChange(mCurDisplayedDate);
            }
        });

        mNextMonthBtn = (ImageButton) v.findViewById(R.id.btn_charts_next_month);
        mPrevMonthBtn = (ImageButton) v.findViewById(R.id.btn_charts_prev_month);

        mNextMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurDisplayedDate.add(Calendar.MONTH, 1);
                notifyDateChange(mCurDisplayedDate);
            }
        });

        mPrevMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurDisplayedDate.add(Calendar.MONTH, -1);
                notifyDateChange(mCurDisplayedDate);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, mCallbacks);
        mDate.setText(MyDateUtils.getFormattedMonthYear(mCurDisplayedDate));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setActionBarTitle();
    }

    private void setActionBarTitle(){
        ((MainActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.nav_charts));
    }

    private void resetExpensesAndCheckForNewCategories(){
        Cursor categories = db.getCategories(); // Fetch all categories
        // initialize hashmaps for expense amounts and category mappings
        if (expensesByCategory != null) expensesByCategory.clear();
        if (mapCategoryNameToCategory != null) mapCategoryNameToCategory.clear();
        for (categories.moveToFirst(); !categories.isAfterLast(); categories.moveToNext()){
            ExpenseCategory category = ExpenseCategory.fromCursor(categories);
            expensesByCategory.put(category, 0.0f);
            mapCategoryNameToCategory.put(category.getName(), category);
        }
    }

    private void notifyDateChange(Calendar cal){
        selectionArgs = new String[] {MyDateUtils.convertMonthYear(cal)};
        getLoaderManager().restartLoader(LOADER_ID, null, mCallbacks);
        mDate.setText(MyDateUtils.getFormattedMonthYear(cal));
    }

    private void updateChartData(Cursor c){
        resetExpensesAndCheckForNewCategories();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String categoryName = c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
            ExpenseCategory curCategory = mapCategoryNameToCategory.get(categoryName);
            expensesByCategory.put(curCategory, expensesByCategory.get(curCategory) + (float)amount);
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        double totalAmount = 0;
        for (Map.Entry expense : expensesByCategory.entrySet()) {
            // turn your data into Entry objects
            if ((Float)expense.getValue() > 0) {
                float amount = (Float) expense.getValue();
                ExpenseCategory category = (ExpenseCategory)expense.getKey();
                entries.add(new PieEntry(amount, category.getName()));
                colors.add(Color.parseColor(category.getColour()));
                totalAmount += amount;
            }
        }

        if (entries.size() > 0){
            PieDataSet dataSet = new PieDataSet(entries, null);
            dataSet.setColors(colors);
            PieData data = new PieData(dataSet);
            mPieChart.setData(data);
            mPieChart.setCenterText(String.format(getResources().getString(R.string.dollar_amount),
                    Utils.formatDoubleTwoDecimalPlaces(totalAmount)));
            mPieChart.spin(0, mPieChart.getRotationAngle(), -90f, Easing.EasingOption.EaseInCirc);
            mPieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        } else {
            mPieChart.clear();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.getContext(), DBContentProvider.CONTENT_URI, PROJECTION, SELECTION, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the Adapter.
                updateChartData(cursor);
                break;
            default:
                return;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
