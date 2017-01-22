package com.danielkim.expensemanager.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.danielkim.expensemanager.Utils.CustomAnimation;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Daniel on 2/14/2016.
 * Add a new expense
 */
public class AddExpenseActivity extends AppCompatActivity
    implements DatePickerDialog.OnDateSetListener{
    private FloatingActionButton fabDone = null;
    DBHelper db;

    // add_expense_header
    private TextView txtExpenseInput = null; // User inputs the expense amount
    private LinearLayout layoutNumPad = null; // Layout of the numpad
    private Button btnCancel = null; // exit activity without adding expense

    //add_expense_body
    private Spinner spinnerCategory = null; // Select expense category
    private Spinner spinnerPaymentMethod = null; // Select payment method
    private EditText txtNotes = null; // Add note
    private EditText txtDate = null; // Expense date. Defaults to current date. Clicking opens CalendarView

    //numpad
    private Button btnExpandNumPad = null;
    private ImageButton btnBackspace = null;
    private String strExpenseTotal = ""; //stores string of user inputted expense
    private Calendar calendar;

    public AddExpenseActivity() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        db = new DBHelper(this);
        calendar = Calendar.getInstance();

        fabDone = (FloatingActionButton)findViewById(R.id.fab_done);
        txtExpenseInput = (TextView)findViewById(R.id.txt_expense_total);
        //btnExpandNumPad = (TextView)findViewById(R.id.expand_numpad);
        btnCancel = (Button)findViewById(R.id.btn_cancel);
        layoutNumPad = (LinearLayout)findViewById(R.id.add_expense_numpad);
        btnExpandNumPad = (Button)findViewById(R.id.btn_okay);
        spinnerCategory = (Spinner)findViewById(R.id.spinner_category);
        spinnerPaymentMethod = (Spinner)findViewById(R.id.spinner_payment_method);
        txtNotes = (EditText)findViewById(R.id.add_expense_note);
        txtDate = (EditText)findViewById(R.id.add_expense_date);
        btnBackspace = (ImageButton)findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strExpenseTotal.length() > 0) {
                    strExpenseTotal = strExpenseTotal.substring(0, strExpenseTotal.length() - 1);
                    if (strExpenseTotal.length() == 0){
                        txtExpenseInput.setText("0");
                    } else {
                        txtExpenseInput.setText(strExpenseTotal);
                    }
                }
            }
        });

        txtExpenseInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (layoutNumPad.getVisibility() == View.GONE){
                    onNumPadExpand();
                } else {
                    onNumPadCollapse();
                }
            }
        });

        btnExpandNumPad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (layoutNumPad.getVisibility() == View.GONE){
                    onNumPadExpand();
                } else {
                    onNumPadCollapse();
                }
            }
        });

        // close activity
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDate();
            }
        });

        txtNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    Utils.hideKeyboard((Activity)getApplicationContext());
                }
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAddExpense();
            }
        });
        fabDone.hide();
        // populate spinners with database data
        populateSpinners();
    }

    private void onNumPadExpand(){
        CustomAnimation.expand(findViewById(R.id.add_expense_numpad));
        btnExpandNumPad.setText(R.string.okay);
        btnExpandNumPad.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
        fabDone.hide();
    }

    private void onNumPadCollapse(){
        CustomAnimation.collapse(findViewById(R.id.add_expense_numpad));
        Drawable img = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_expand_more_white_24dp);
        btnExpandNumPad.setText("");
        btnExpandNumPad.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        fabDone.show();
    }

    public void onNumPadButtonClick(View v){
        int integerPlaces = strExpenseTotal.indexOf('.');
        if (integerPlaces != -1){
            int decimalPlaces = strExpenseTotal.length() - integerPlaces - 1;
            if (decimalPlaces >= 2){
                // do not accept more input if there are already 2 decimal places
                return ;
            }
        }

        Button btn = (Button)findViewById(v.getId());
        if (strExpenseTotal.length() == 0){
            if (btn.getId() == R.id.btnDot){
                // first button press is a decimal. append a 0 in front.
                strExpenseTotal = "0";
            } else if (btn.getId() == R.id.btn0){
                // first button press is a 0. Don't update display or update strExpenseTotal
                // (leading 0 does not change value)
                return;
            }
        }

        strExpenseTotal += btn.getText();
        txtExpenseInput.setText(strExpenseTotal);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String date = (monthOfYear + 1) + " / " + dayOfMonth + " / " + year;
        txtDate.setText(date);

        // update calendar
        calendar.set(year, monthOfYear, dayOfMonth);
    }

    // Open CalendarView to choose date
    private void chooseDate(){
        Utils.hideKeyboard(this);

        DatePickerDialog datePicker = new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // User clicks doneFab button to add expense
    private void confirmAddExpense(){
        double amount = Double.parseDouble(txtExpenseInput.getText().toString());
        if (amount == 0){
            // expense cannot be 0.
            // Open snackbar to notify user
            Snackbar.make(findViewById(R.id.fab_done), getString(R.string.snackbar_input_amount), Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        String notes = txtNotes.getText().toString();
        Cursor category = ((Cursor) spinnerCategory.getSelectedItem());
        long categoryId = category.getInt(category.getColumnIndex(DBHelper.CategoriesTable._ID));
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();
        // Store date as time since epoch
        long date = calendar.getTimeInMillis();
        //db.insertNewExpense(date, amount, categoryId, paymentMethod, notes);

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.ExpensesTable.COL_AMOUNT, amount);
        cv.put(DBHelper.ExpensesTable.COL_DATE, date);
        cv.put(DBHelper.ExpensesTable.COL_CATEGORY_ID, categoryId);
        cv.put(DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID, paymentMethod);
        cv.put(DBHelper.ExpensesTable.COL_NOTES, notes);
        getContentResolver().insert(DBContentProvider.CONTENT_URI, cv);

        onBackPressed(); // close activity
    }

    // Populate spinners with the categories and payment methods
    private void populateSpinners(){
        Cursor categoriesCursor = db.getCategories();
        Cursor pmCursor = db.getPaymentMethods();

        ArrayList<String> paymentMethods = new ArrayList<>();

        for (pmCursor.moveToFirst(); !pmCursor.isAfterLast(); pmCursor.moveToNext()){
            paymentMethods.add(pmCursor.getString(pmCursor.getColumnIndex(DBHelper.PaymentMethodsTable.COL_PAYMENT_METHOD)));
        }

        SimpleCursorAdapter adapterCategories = new SimpleCursorAdapter
                (this, android.R.layout.simple_dropdown_item_1line, categoriesCursor,
                        new String[] {DBHelper.CategoriesTable.COL_CATEGORY},
                        new int[] {android.R.id.text1}, 0);
        spinnerCategory.setAdapter(adapterCategories);

        ArrayAdapter<String> adapterPaymentMethods = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentMethods);
        spinnerPaymentMethod.setAdapter(adapterPaymentMethods);

        pmCursor.close();
    }
}
