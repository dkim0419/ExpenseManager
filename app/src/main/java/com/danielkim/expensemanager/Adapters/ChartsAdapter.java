package com.danielkim.expensemanager.Adapters;

/**
 * Created by Daniel on 2/2/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.danielkim.expensemanager.Fragments.ChartsFragment;

public class ChartsAdapter extends CursorRecyclerViewAdapter {
    public ChartsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }
}