package com.danielkim.expensemanager.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
        item = getIntent().getParcelableExtra(INTENT_EXPENSE_ITEM);
        mDateTextView = (TextView) findViewById(R.id.txt_view_expense_date);
        mAmountTextView = (TextView) findViewById(R.id.txt_view_expense_amount);
        mPaymentMethodTextView = (TextView) findViewById(R.id.txt_view_expense_payment_method);
        mCategoryTextView = (TextView) findViewById(R.id.txt_view_expense_category);
        mNoteTextView = (TextView) findViewById(R.id.txt_view_expense_note);

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

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setBackgroundDrawable(new ColorDrawable(mCategoryColor));
            actionBar.setTitle("");
            // Add back button to go back to MainActivity
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }
}
