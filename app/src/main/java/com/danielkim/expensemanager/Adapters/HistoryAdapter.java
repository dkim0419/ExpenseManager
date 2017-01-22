package com.danielkim.expensemanager.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.danielkim.expensemanager.Models.ExpenseItem;
import com.danielkim.expensemanager.R;
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
        ViewHolder(View view) {
            super(view);
            categoryTxt = (TextView)view.findViewById(R.id.txt_hist_category);
            amountTxt = (TextView)view.findViewById(R.id.txt_hist_amount);
            dateTxt = (TextView)view.findViewById(R.id.txt_hist_date);
            paymentMethodTxt = (TextView)view.findViewById(R.id.txt_hist_payment_method);
            circle = (ImageView)view.findViewById(R.id.circle_hist);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater
                .inflate(R.layout.li_history_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        ExpenseItem item = ExpenseItem.fromCursor(cursor);
        String note = item.getNote();
        viewHolder.categoryTxt.setText(note.isEmpty() ? item.getCategory().getName() : note);
        viewHolder.amountTxt.setText("$" + Utils.formatDoubleTwoDecimalPlaces(item.getAmount()));
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.CANADA);
        viewHolder.dateTxt.setText(DateUtils.formatDateTime(
                mContext,
                item.getDateMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
        ));

        viewHolder.paymentMethodTxt.setText(item.getPaymentMethod());
        viewHolder.circle.setColorFilter(Color.parseColor(item.getCategory().getColour()));
    }
}
