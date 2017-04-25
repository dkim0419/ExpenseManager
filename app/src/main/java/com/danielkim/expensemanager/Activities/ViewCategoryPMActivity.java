package com.danielkim.expensemanager.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.danielkim.expensemanager.Adapters.ViewCategoryPMAdapter;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Dialogs.AddEditCategoryPMDialog;
import com.danielkim.expensemanager.R;

/**
 * Created by Daniel on 4/12/2017.
 */

public class ViewCategoryPMActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static String VIEW_TYPE = "viewType";

    private static final String[] PROJECTION_CATEGORY =
            new String[] {
                    DBHelper.CategoriesTable._ID,
                    DBHelper.CategoriesTable.COL_CATEGORY,
                    DBHelper.CategoriesTable.COL_COLOUR,
                    DBHelper.CategoriesTable.COL_VISIBLE,
            };

    private static final String SELECTION_CATEGORY = DBHelper.CategoriesTable.COL_VISIBLE + " = 1";

    private RecyclerView mRecyclerView;
    private ViewType mViewType;
    private ViewCategoryPMAdapter mAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private static final int LOADER_ID = 0;

    public enum ViewType {
        Category,
        PaymentMethod
    }

    public ViewCategoryPMActivity() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category_pm);
        mRecyclerView = (RecyclerView) findViewById(R.id.items_list);
        Bundle bundle = getIntent().getExtras();
        mViewType = (ViewType) bundle.get(VIEW_TYPE);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                llm.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ViewCategoryPMAdapter(this, null, ViewType.Category);
        mRecyclerView.setAdapter(mAdapter);
        mCallbacks = this;
        getSupportLoaderManager().initLoader(LOADER_ID, null, mCallbacks);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if(mViewType == ViewType.Category) actionBar.setTitle(R.string.pref_add_category_title);
            else actionBar.setTitle(R.string.pref_add_payment_method_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_new_category_pm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_new_category_pm) {
            AddEditCategoryPMDialog dialog = AddEditCategoryPMDialog.newInstance(null);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            dialog.show(transaction, "add_edit_category_pm_dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DBContentProvider.CONTENT_URI_CATEGORIES, PROJECTION_CATEGORY, SELECTION_CATEGORY, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the Adapter.
                mAdapter.swapCursor(cursor);
                break;
            default:
                return;
        }
        // The list view now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public ViewType getViewType() {
        return mViewType;
    }
}
