package com.danielkim.expensemanager.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.danielkim.expensemanager.Activities.AddExpenseActivity;
import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Adapters.OverviewAdapter;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Daniel on 2/17/2016.
 */
public class OverviewFragment extends Fragment implements NavDrawerFragment, LoaderManager.LoaderCallbacks<Cursor> {
    private TextView txtCurrentMonth = null;
    private FloatingActionButton fabAddExpense; // add new expense fab button
    private TextView txtCurrentMonthExpenses = null;
    private ScrollView overviewLayout = null;
    private OverviewAdapter adapter = null;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    private static final String[] PROJECTION =
            new String[]
                    {
                        DBHelper.ExpensesTable.COL_AMOUNT,
                    };

    private static final String SELECTION = "strftime('" + Utils.MONTH_YEAR_FORMAT_SQL + "', " + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch') = ?";
    private String[] selectionArgs;

    private static final int LOADER_ID = 0;

    public OverviewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1; // 0 indexed month
        int year = cal.get(Calendar.YEAR);
        selectionArgs = new String[] {String.format("%02d", month) + " " + year};
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
        txtCurrentMonthExpenses = (TextView)v.findViewById(R.id.overview_txt_current_month_expenses);
        overviewLayout = (ScrollView)v.findViewById(R.id.overviewBody);

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the Adapter.

                double sum = 0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                    sum += cursor.getDouble(cursor.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT));
                }

                txtCurrentMonthExpenses.setText(Utils.formatDoubleTwoDecimalPlaces((sum)));
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
