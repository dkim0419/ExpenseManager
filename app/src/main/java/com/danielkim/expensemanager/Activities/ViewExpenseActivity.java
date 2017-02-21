package com.danielkim.expensemanager.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Models.ExpenseItem;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;

/**
 * Created by Daniel on 1/28/2017.
 */

public class ViewExpenseActivity extends AppCompatActivity {
    public static final String INTENT_EXPENSE_ITEM = "VIEW_EXPENSE_ITEM";

    private FloatingActionButton mFab;
    private TextView mDateTextView;
    private TextView mAmountTextView;
    private TextView mPaymentMethodTextView;
    private TextView mCategoryTextView;
    private TextView mNoteTextView;
    private int mCategoryColor;
    private ExpenseItem item;

    public ViewExpenseActivity(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(onFabClicked());

        item = getIntent().getParcelableExtra(INTENT_EXPENSE_ITEM);
        mDateTextView = (TextView) findViewById(R.id.txt_view_expense_date);
        mAmountTextView = (TextView) findViewById(R.id.txt_view_expense_amount);
        mPaymentMethodTextView = (TextView) findViewById(R.id.txt_view_expense_payment_method);
        mCategoryTextView = (TextView) findViewById(R.id.txt_view_expense_category);
        mNoteTextView = (TextView) findViewById(R.id.txt_view_expense_note);

        updateData(item);
        setUpActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DBHelper db = new DBHelper(this);
        // fetch item again in case of updates to data
        ExpenseItem i = db.getItemById(item.getId());
        updateData(i);
    }

    private void setUpActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle("");
            // Add back button to go back to MainActivity
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void updateData(ExpenseItem i){
        if (i == null) return;
        item = i;
        mDateTextView.setText(MyDateUtils.formatDateMillisLong(getApplicationContext(), item.getDateMillis()));
        mAmountTextView.setText(String.format(getResources().getString(R.string.dollar_amount), Utils.formatDoubleTwoDecimalPlaces(item.getAmount())));
        mPaymentMethodTextView.setText(item.getPaymentMethod());
        mCategoryTextView.setText(item.getCategory().getName());
        mNoteTextView.setText(!TextUtils.isEmpty(item.getNote()) ? item.getNote() : getResources().getString(R.string.none));

        mCategoryColor = Color.parseColor(item.getCategory().getColour());
        findViewById(R.id.view_expense_header).setBackgroundColor(mCategoryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.darkenColor(mCategoryColor));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(mCategoryColor));
    }

    private View.OnClickListener onFabClicked(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ViewExpenseActivity.this, AddExpenseActivity.class);
                i.putExtra(AddExpenseActivity.VIEW_EXPENSE_ITEM_INTENT, item);
                ViewExpenseActivity.this.startActivity(i);
            }
        };
    }
}
