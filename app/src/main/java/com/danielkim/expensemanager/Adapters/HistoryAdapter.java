package com.danielkim.expensemanager.Adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.danielkim.expensemanager.Activities.AddExpenseActivity;
import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Activities.ViewExpenseActivity;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Fragments.HistoryFragment;
import com.danielkim.expensemanager.Models.ExpenseItem;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;

/**
 * Created by Daniel on 2/20/2016.
 */

public class HistoryAdapter extends CursorRecyclerViewAdapter<HistoryAdapter.ViewHolder>{
    private Context mContext;
    private LayoutInflater layoutInflater;

    public HistoryAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTxt;
        TextView amountTxt;
        TextView dateTxt;
        TextView paymentMethodTxt;
        ImageView circle;
        LinearLayout layout;
        ViewHolder(View view) {
            super(view);
            categoryTxt = (TextView)view.findViewById(R.id.txt_hist_category);
            amountTxt = (TextView)view.findViewById(R.id.txt_hist_amount);
            dateTxt = (TextView)view.findViewById(R.id.txt_hist_date);
            paymentMethodTxt = (TextView)view.findViewById(R.id.txt_hist_payment_method);
            circle = (ImageView)view.findViewById(R.id.circle_hist);
            layout = (LinearLayout)view.findViewById(R.id.history_item_layout);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater
                .inflate(R.layout.li_history_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
        final ExpenseItem item = ExpenseItem.fromCursor(cursor, mContext);
        String note = item.getNote();
        viewHolder.categoryTxt.setText(note.isEmpty() ? item.getCategory().getName() : note);
        viewHolder.amountTxt.setText(String.format(mContext.getResources().getString(R.string.dollar_amount), Utils.formatDoubleTwoDecimalPlaces(item.getAmount())));
        viewHolder.dateTxt.setText(MyDateUtils.formatDateMillis(mContext, item.getDateMillis()));
        viewHolder.paymentMethodTxt.setText(item.getPaymentMethod());
        viewHolder.circle.setColorFilter(Color.parseColor(item.getCategory().getColour()));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ViewExpenseActivity.class);
                i.putExtra(ViewExpenseActivity.INTENT_EXPENSE_ITEM, item);
                mContext.startActivity(i);
            }
        });

        viewHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            final CharSequence[] items = new CharSequence[]{
                    mContext.getResources().getString(R.string.edit_expense),
                    mContext.getResources().getString(R.string.delete_expense),
            };

            @Override
            public boolean onLongClick(View v) {
                final View view = v;
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.dialog_options_title)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    openEditExpenseActivity(item);
                                } else if (which == 1){
                                    deleteExpenseItem(item, view, viewHolder.getAdapterPosition());
                                }
                            }
                        }).show();
                return false;
            }
        });
    }

    private void deleteExpenseItem(final ExpenseItem item, View view, final int position){
        final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI, item.getId());
        mContext.getContentResolver().delete(uri, null, null);

        Snackbar.make(view, mContext.getString(R.string.snackbar_expense_deleted), Snackbar.LENGTH_LONG)
                .setAction(mContext.getString(R.string.snackbar_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // re-insert item into database
                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.ExpensesTable._ID, item.getId());
                        cv.put(DBHelper.ExpensesTable.COL_AMOUNT, item.getAmount());
                        cv.put(DBHelper.ExpensesTable.COL_DATE, item.getDateMillis());
                        cv.put(DBHelper.ExpensesTable.COL_CATEGORY_ID, item.getCategory().getId());
                        cv.put(DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID, item.getPaymentMethod());
                        cv.put(DBHelper.ExpensesTable.COL_NOTES, item.getNote());
                        mContext.getContentResolver().insert(DBContentProvider.CONTENT_URI, cv);
                    }
                }).show();
    }

    private void openEditExpenseActivity(ExpenseItem item){
        Intent i = new Intent(mContext, AddExpenseActivity.class);
        i.putExtra(AddExpenseActivity.VIEW_EXPENSE_ITEM_INTENT, item);
        mContext.startActivity(i);
    }

    @Override
    protected void notifyCursorDataChanged(Cursor oldCursor, Cursor newCursor) {
        if (oldCursor == null || newCursor == null || oldCursor.getCount() == newCursor.getCount()){
            notifyDataSetChanged();
            return;
        }

        boolean itemsInserted = oldCursor.getCount() < newCursor.getCount();
        // iterate on larger count cursor, compare to smaller count cursor
        Cursor iterateCursor = oldCursor;
        Cursor compareCursor = newCursor;
        if (itemsInserted){
            iterateCursor = newCursor;
            compareCursor = oldCursor;
        }

        iterateCursor.moveToFirst();
        compareCursor.moveToFirst();
        boolean notifyRemaining = false;
        // checking for items removed/added
        int i = 0;
        while (!iterateCursor.isAfterLast() && !compareCursor.isAfterLast()){
            if (iterateCursor.getLong(iterateCursor.getColumnIndex(DBHelper.ExpensesTable._ID))
                    != compareCursor.getLong(compareCursor.getColumnIndex(DBHelper.ExpensesTable._ID))) {
                if (itemsInserted) notifyItemInserted(i);
                else notifyItemRemoved(i);
                iterateCursor.moveToNext();
            }

            iterateCursor.moveToNext();
            compareCursor.moveToNext();
            i++;
        }

        int k = i;
        while (!compareCursor.isAfterLast()){
            if (itemsInserted) notifyItemInserted(i);
            else notifyItemRemoved(i);
            compareCursor.moveToNext();
            i++;
        }

        while (!iterateCursor.isAfterLast()){
            if (itemsInserted) notifyItemInserted(i);
            else notifyItemRemoved(k);
            iterateCursor.moveToNext();
            k++;
        }
    }
}
