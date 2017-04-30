package com.danielkim.expensemanager.Adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danielkim.expensemanager.Activities.ViewCategoryPMActivity;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Dialogs.AddEditCategoryPMDialog;
import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.Models.ExpensePaymentMethod;
import com.danielkim.expensemanager.R;

/**
 * Created by Daniel on 4/12/2017.
 */

public class ViewCategoryPMAdapter extends CursorRecyclerViewAdapter<ViewCategoryPMAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflator;
    private ViewCategoryPMActivity.ViewType mViewType;

    public ViewCategoryPMAdapter(Context context, Cursor cursor, ViewCategoryPMActivity.ViewType type) {
        super(context, cursor);
        mContext = context;
        mLayoutInflator = LayoutInflater.from(context);
        mViewType = type;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        ImageView mCircle;
        ImageButton mDeleteButton;
        LinearLayout mLayout;
        ViewHolder(View view) {
            super(view);
            mTitle = (TextView)view.findViewById(R.id.title);
            mCircle = (ImageView) view.findViewById(R.id.circle);
            mDeleteButton = (ImageButton)view.findViewById(R.id.btn_delete);
            mLayout = (LinearLayout)view.findViewById(R.id.edit_category_pm_layout);
        }
    }

    @Override
    public void onBindViewHolder(final ViewCategoryPMAdapter.ViewHolder viewHolder, Cursor c) {
        if (mViewType == ViewCategoryPMActivity.ViewType.Category) {
            final ExpenseCategory category = ExpenseCategory.fromCursor(c);
            viewHolder.mTitle.setText(category.getName());
            viewHolder.mCircle.setColorFilter(Color.parseColor(category.getColour()));
            // delete a category / pm
            viewHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCategory (category, v, viewHolder.getAdapterPosition());
                }
            });
            viewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // edit details
                    AddEditCategoryPMDialog dialog = AddEditCategoryPMDialog.newInstance(category, null);
                    FragmentTransaction transaction = ((ViewCategoryPMActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    dialog.show(transaction, "add_edit_category_pm_dialog");
                }
            });
        } else {
            final ExpensePaymentMethod pm = ExpensePaymentMethod.fromCursor(c);
            viewHolder.mTitle.setText(pm.getName());
            viewHolder.mCircle.setColorFilter(R.color.gray);
            viewHolder.mCircle.setImageResource(R.drawable.ic_card_black_24dp);
            // delete a category / pm
            viewHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePaymentMethod (pm, v, viewHolder.getAdapterPosition());
                }
            });
            viewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // edit details
                    AddEditCategoryPMDialog dialog = AddEditCategoryPMDialog.newInstance(null, pm);
                    FragmentTransaction transaction = ((ViewCategoryPMActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    dialog.show(transaction, "add_edit_category_pm_dialog");
                }
            });
        }
    }

    @Override
    public ViewCategoryPMAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflator
                .inflate(R.layout.li_edit_category_pm, parent, false);
        return new ViewHolder(itemView);
    }

    private void deleteCategory(final ExpenseCategory category, View view, final int position){
        final ContentValues cv = new ContentValues();
        cv.put(DBHelper.CategoriesTable._ID, category.getId());
        cv.put(DBHelper.CategoriesTable.COL_COLOUR, category.getColour());
        cv.put(DBHelper.CategoriesTable.COL_CATEGORY, category.getName());
        cv.put(DBHelper.CategoriesTable.COL_VISIBLE, 0); // hide visibility

        final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI_CATEGORIES, category.getId());
        mContext.getContentResolver().update(uri, cv, null, null);

        Snackbar.make(view, mContext.getString(R.string.snackbar_category_deleted, category.getName()), Snackbar.LENGTH_LONG)
                .setAction(mContext.getString(R.string.snackbar_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // make category visible again
                        cv.put(DBHelper.CategoriesTable.COL_VISIBLE, 1); // re-enable visibility
                        mContext.getContentResolver().update(uri, cv, null, null);
                    }
                }).show();
    }

    private void deletePaymentMethod(final ExpensePaymentMethod pm, View view, final int position){
        final ContentValues cv = new ContentValues();
        cv.put(DBHelper.PaymentMethodsTable._ID, pm.getId());
        cv.put(DBHelper.PaymentMethodsTable.COL_COLOUR, pm.getColour());
        cv.put(DBHelper.PaymentMethodsTable.COL_PAYMENT_METHOD, pm.getName());
        cv.put(DBHelper.PaymentMethodsTable.COL_VISIBLE, 0); // hide visibility

        final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI_PAYMENT_METHODS, pm.getId());
        mContext.getContentResolver().update(uri, cv, null, null);

        Snackbar.make(view, mContext.getString(R.string.snackbar_pm_deleted, pm.getName()), Snackbar.LENGTH_LONG)
                .setAction(mContext.getString(R.string.snackbar_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // make category visible again
                        cv.put(DBHelper.PaymentMethodsTable.COL_VISIBLE, 1); // re-enable visibility
                        mContext.getContentResolver().update(uri, cv, null, null);
                    }
                }).show();
    }
}
