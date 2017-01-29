package com.danielkim.expensemanager.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.danielkim.expensemanager.Activities.MainActivity;
import com.danielkim.expensemanager.Fragments.HistoryFilterFragment;
import com.danielkim.expensemanager.Fragments.HistoryFragment;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;

/**
 * Created by Daniel on 11/7/2016.
 */
public class HistoryFilterAdapter extends CursorRecyclerViewAdapter<HistoryFilterAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater layoutInflater;

    public HistoryFilterAdapter(Context context, Cursor c) {
        super(context, c);
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mAmount;
        private TextView mDate;
        private TextView mCount;
        private CardView mCardView;

        ViewHolder(View itemView) {
            super(itemView);
            mAmount = (TextView)itemView.findViewById(R.id.txt_hist_filter_amount);
            mDate = (TextView)itemView.findViewById(R.id.txt_hist_filter_month);
            mCount = (TextView)itemView.findViewById(R.id.txt_hist_filter_count);
            mCardView = (CardView) itemView.findViewById(R.id.hist_filter_cardview);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater
                .inflate(R.layout.li_history_filter_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        final String monthYear = c.getString(HistoryFilterFragment.PROJECTION_DATE);
        String monthYearFormatted = Utils.getFormattedMonthYear( c.getString(HistoryFilterFragment.PROJECTION_DATE));
        String amount = Utils.formatDoubleTwoDecimalPlaces(c.getDouble(HistoryFilterFragment.PROJECTION_SUM)); // amount
        int count = c.getInt(HistoryFilterFragment.PROJECTION_COUNT);

        holder.mAmount.setText("$" + amount);
        holder.mDate.setText(monthYearFormatted);
        holder.mCount.setText(String.format(mContext.getResources().getString(R.string.total_transactions), count));

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryFragment fragment = HistoryFragment.newInstance(monthYear);
                ((MainActivity)mContext).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }
        });
    }
}
