package com.danielkim.expensemanager.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;

import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Adapters.HistoryAdapter;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;

/**
 * Created by Daniel on 4/21/2016.
 */
public class HistoryFragment extends Fragment implements NavDrawerFragment, LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView mRecyclerView;
    private HistoryAdapter adapter;

    private static final int LOADER_ID = 0;
    private static final String[] PROJECTION =
            new String[]
                {
                        "t1." + DBHelper.ExpensesTable._ID,
                        "t1." + DBHelper.ExpensesTable.COL_DATE,
                        "t1." + DBHelper.ExpensesTable.COL_AMOUNT,
                        "t2." + DBHelper.CategoriesTable.COL_CATEGORY,
                        "t2." + DBHelper.CategoriesTable.COL_COLOUR,
                        "t1." + DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID,
                        "t1." + DBHelper.ExpensesTable.COL_NOTES
                };
    private static final String SORT_BY = "t1." + DBHelper.ExpensesTable.COL_DATE + " DESC";
    private static final String SELECTION = "strftime('" + Utils.MONTH_YEAR_FORMAT_SQL + "', t1." + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch') = ?";
    private String[] selectionArgs;

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private static final String ARGS_MONTH_YEAR = "monthYear";
    private String monthYear;

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance(String monthYear) {
        Bundle args = new Bundle();
        HistoryFragment fragment = new HistoryFragment();
        args.putString(ARGS_MONTH_YEAR, monthYear);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getNavDrawerId() {
        return R.id.nav_history;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // set month year to display
        Bundle args = getArguments();
        monthYear = args.getString(ARGS_MONTH_YEAR);
        selectionArgs = new String[] {monthYear};
        setActionBarTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        setActionBarTitle();
    }

    private void setActionBarTitle(){
        String title = Utils.getFormattedMonthYear(monthYear);
        ((MainActivity) getActivity()).setActionBarTitle(title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.historyRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new HistoryAdapter(this.getContext(), null);
        mRecyclerView.setAdapter(adapter);
        mCallbacks = this;
        getLoaderManager().initLoader(LOADER_ID, null, mCallbacks);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_filter_history:
                HistoryFilterFragment fragment = new HistoryFilterFragment();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.getContext(), DBContentProvider.CONTENT_URI, PROJECTION, SELECTION, selectionArgs, SORT_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the Adapter.
                adapter.swapCursor(cursor);
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
        adapter.swapCursor(null);
    }
}