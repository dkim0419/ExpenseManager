package com.danielkim.expensemanager.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.danielkim.expensemanager.Activities.AddExpenseActivity;
import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Adapters.OverviewAdapter;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.lb.auto_fit_textview.AutoResizeTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 2/17/2016.
 */
public class OverviewFragment extends Fragment implements INavDrawerFragment, LoaderManager.LoaderCallbacks<Cursor> {
    private TextView txtCurrentMonth = null;
    private FloatingActionButton fabAddExpense; // add new expense fab button
    private AutoResizeTextView txtCurrentMonthExpenses = null;
    private ViewGroup overviewLayout = null;
    private OverviewAdapter adapter = null;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    private static final String[] PROJECTION =
        new String[]
                {
                        "strftime('" + MyDateUtils.MONTH_YEAR_FORMAT_SQL + "', t1." + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch','localtime')",
                        "t1." + DBHelper.ExpensesTable.COL_AMOUNT,
                        "t2." + DBHelper.CategoriesTable.COL_CATEGORY,
                        "t1." + DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID,
                        "t3." + DBHelper.PaymentMethodsTable.COL_PAYMENT_METHOD,
                };

    private static final String SELECTION = "strftime('" + MyDateUtils.MONTH_YEAR_FORMAT_SQL + "', t1." + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch','localtime') = ?";
    private String[] selectionArgs;

    private static final int LOADER_ID = 0;

   // private HorizontalBarChart mBarChart;
    private PieChart mPieChart;
    private Map<ExpenseCategory, Float> expensesByCategory;
    private HashMap<String, ExpenseCategory> mapCategoryNameToCategory;
    private List<Integer> colors;
    //private List<String> labels;
    private DBHelper db;
    private double totalAmount = 0;

    public OverviewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectionArgs = new String[] {MyDateUtils.convertMonthYear(Calendar.getInstance())};
        db = new DBHelper(getContext());
        expensesByCategory = new HashMap<>();
        mapCategoryNameToCategory = new HashMap<>();
        colors = new ArrayList<>();
        //labels = new ArrayList<>();
    }

    @Override
    public int getNavDrawerId() {
        return R.id.nav_overview;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        txtCurrentMonth = (TextView)v.findViewById(R.id.overview_txt_current_month);
        txtCurrentMonthExpenses = (AutoResizeTextView)v.findViewById(R.id.overview_txt_current_month_expenses);
        overviewLayout = (ViewGroup) v.findViewById(R.id.overviewBody);

        String monthName =(String)android.text.format.DateFormat.format("MMMM", new Date());
        txtCurrentMonth.setText(monthName);

        fabAddExpense = (FloatingActionButton)v.findViewById(R.id.fab);
        if (fabAddExpense != null) {
            fabAddExpense.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    addNewExpense();
                }
            });
        }

        /*
        mBarChart = new HorizontalBarChart(getContext());
        mBarChart.getLegend().setEnabled(false);
        mBarChart.setDescription(null);
        mBarChart.getXAxis().setDrawLabels(true);
        mBarChart.getAxisLeft().setDrawLabels(false);
        mBarChart.getAxisRight().setDrawLabels(false);
        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.getAxisRight().setDrawGridLines(false);
        mBarChart.getXAxis().setDrawGridLines(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.getXAxis().setDrawAxisLine(false);
        mBarChart.getAxisRight().setDrawAxisLine(false);
        mBarChart.getAxisLeft().setDrawAxisLine(false);
        //mBarChart.setScaleEnabled(false);
        overviewLayout.addView(mBarChart);
        */

        mPieChart = new PieChart(getContext());
        mPieChart.setNoDataText(getResources().getString(R.string.chart_no_data));
        mPieChart.setNoDataTextColor(Color.DKGRAY);
        mPieChart.setRotationEnabled(false);
        mPieChart.getLegend().setEnabled(false);
        mPieChart.setHoleRadius(65);
        mPieChart.setDescription(null);
        mPieChart.setEntryLabelColor(Color.WHITE);
        mPieChart.setUsePercentValues(true);
        mPieChart.setDrawEntryLabels(false);
        mPieChart.setExtraOffsets(20, 10, 20, 10);
        mPieChart.setCenterTextSize(25);
        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                setPieChartCenterText(e.getY(), ((PieEntry)e).getLabel(), (int)h.getX());
            }

            @Override
            public void onNothingSelected() {
                setPieChartCenterText(totalAmount, getResources().getString(R.string.chart_total), -1);
            }
        });
        mPieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        overviewLayout.addView(mPieChart);

        mCallbacks = this;
        getLoaderManager().initLoader(LOADER_ID, null, mCallbacks);
        return v;
    }

    public void addNewExpense(){
        Intent intent = new Intent(this.getActivity(), AddExpenseActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.getContext(), DBContentProvider.CONTENT_URI, PROJECTION, SELECTION, selectionArgs, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setActionBarTitle(getResources().getString(R.string.nav_overview));
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

    /*
    private void updateChartData(Cursor c){
        resetExpensesAndCheckForNewCategories();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String categoryName = c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
            ExpenseCategory curCategory = mapCategoryNameToCategory.get(categoryName);
            expensesByCategory.put(curCategory, expensesByCategory.get(curCategory) + (float)amount);
        }

        List<BarEntry> entries = new ArrayList<>();
        if (labels != null) labels.clear();
        if (colors != null) colors.clear();
        totalAmount = 0;
        int i = 0;
        expensesByCategory = Utils.sortByValue(expensesByCategory);
        for (Map.Entry expense : expensesByCategory.entrySet()) {
            // turn your data into Entry objects
            float amount = (Float) expense.getValue();
            if (amount > 0) {
                ExpenseCategory category = (ExpenseCategory)expense.getKey();
                entries.add(new BarEntry(i, new float[] {amount}, category.getName()));
                labels.add(category.getName());
                colors.add(Color.parseColor(category.getColour()));
                totalAmount += amount;
            }
            i++;
        }

        if (entries.size() > 0){
            BarDataSet dataSet = new BarDataSet(entries, null);
            dataSet.setHighLightAlpha(0);
            dataSet.setColors(colors);

            dataSet.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return "";
                    return String.format(
                            getResources().getString(R.string.dollar_amount),
                            Utils.formatDoubleTwoDecimalPlaces(value));
                }
            });

            BarData data = new BarData(dataSet);
            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if (value < 0 || value >= labels.size()) {
                        return "";
                    }
                    return labels.get((int) value);
                }
            };
            mBarChart.getXAxis().setValueFormatter(formatter);
            mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(10);
            mBarChart.setData(data);
            mBarChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        } else {
            mBarChart.clear();
        }
    }*/

    private void updateChartData(Cursor c){
        resetExpensesAndCheckForNewCategories();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            String categoryName = c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_CATEGORY));
            double amount = c.getDouble(c.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
            ExpenseCategory curCategory = mapCategoryNameToCategory.get(categoryName);
            expensesByCategory.put(curCategory, expensesByCategory.get(curCategory) + (float)amount);
        }

        List<PieEntry> entries = new ArrayList<>();
        if (colors != null) colors.clear();
        totalAmount = 0;
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
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(10);
            mPieChart.setData(data);
            setPieChartCenterText(totalAmount, getResources().getString(R.string.chart_total), -1);

            mPieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        } else {
            mPieChart.clear();
        }
    }

    private void setPieChartCenterText(double amount, String category, int colorIndex){
        int centerLabelColor;
        int centerDataColor;

        if (colorIndex != -1 && colors != null && colorIndex < colors.size()){
            centerLabelColor = colors.get(colorIndex);
            centerDataColor = colors.get(colorIndex);
            mPieChart.setCenterTextSize(25);
        } else {
            // TODO: Emoji changes based on user's set monthly budget
            mPieChart.setCenterText("☺");
            mPieChart.setCenterTextSize(50);
            mPieChart.setCenterTextColor(Color.BLACK);
            return;
        }

        String amountText = String.format(category.toUpperCase() + "\n$%s",
                Utils.formatDoubleTwoDecimalPlaces(amount));
        int index = amountText.indexOf("\n");
        Spannable ss = new SpannableString(amountText);
        ss.setSpan(new RelativeSizeSpan(0.5f), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(centerLabelColor),
                0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mPieChart.setCenterText(ss);
        mPieChart.setCenterTextColor(centerDataColor);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the Adapter.
/*
                double sum = 0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                    sum += cursor.getDouble(cursor.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
                }
                SpannableString currentExpenses =
                        new SpannableString(
                                String.format(getResources().getString(R.string.dollar_amount),
                                        Utils.formatDoubleTwoDecimalPlaces((sum))));
                currentExpenses.setSpan(new RelativeSizeSpan(0.5f), 0, 1, 0);
                txtCurrentMonthExpenses.setText(currentExpenses);*/
                updateChartData(cursor);
                SpannableString currentExpenses =
                        new SpannableString(
                                String.format(getResources().getString(R.string.dollar_amount),
                                        Utils.formatDoubleTwoDecimalPlaces((totalAmount))));
                currentExpenses.setSpan(new RelativeSizeSpan(0.5f), 0, 1, 0);
                txtCurrentMonthExpenses.setText(currentExpenses);
                break;
            default:
                return;
        }
        // The list view now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        return;
    }
}
