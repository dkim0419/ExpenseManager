package com.danielkim.expensemanager.Fragments;

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

    private PieChart mPieChart;
    private ImageButton mNextMonthBtn;
    private ImageButton mPrevMonthBtn;
    private TextView mDate;

    private Calendar mCurDisplayedDate; // month-year of displayed chart data
    private Calendar mTodayDate; // month year of current date

    private HashMap<String, Float> expensesByCategory;
    private ArrayList<Integer> categoryColors;
    private ArrayList<ExpenseCategory> allCategories;

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
        allCategories = new ArrayList<>();
        categoryColors = new ArrayList<>();
        // initialize hashmap with all categories
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            ExpenseCategory category = ExpenseCategory.fromCursor(c);
            expensesByCategory.put(category.getName(), 0.0f);
            allCategories.add(category);
            categoryColors.add(Color.parseColor(category.getColour()));
        }

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

        FrameLayout layout = (FrameLayout) v.findViewById(R.id.charts_container);
        mPieChart = new PieChart(getContext());
        layout.addView(mPieChart);

        mDate = (TextView) v.findViewById(R.id.txt_charts_date);
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurDisplayedDate = mTodayDate; // reset display month to today
            }
        });

        mNextMonthBtn = (ImageButton) v.findViewById(R.id.btn_charts_next_month);
        mPrevMonthBtn = (ImageButton) v.findViewById(R.id.btn_charts_prev_month);

        mNextMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPrevMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);

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

    private void updateChartData(Cursor c){
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String categoryName = c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
            expensesByCategory.put(categoryName, expensesByCategory.get(categoryName) + (float)amount);
        }

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry expense : expensesByCategory.entrySet()) {
            // turn your data into Entry objects
            if ((Float)expense.getValue() > 0) {
                entries.add(new PieEntry((Float) expense.getValue(), (String)expense.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, null); // TODO: pass in title for data set
        dataSet.setColors(categoryColors);
        PieData data = new PieData(dataSet);
        mPieChart.setData(data);
        mPieChart.invalidate();
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
