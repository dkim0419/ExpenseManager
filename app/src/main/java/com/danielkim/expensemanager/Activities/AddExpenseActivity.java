package com.danielkim.expensemanager.Activities;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.Models.ExpenseItem;
import com.danielkim.expensemanager.Utils.CustomAnimation;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;
import com.lb.auto_fit_textview.AutoResizeTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.view.View.GONE;

/**
 * Created by Daniel on 2/14/2016.
 * Add a new expense
 */
public class AddExpenseActivity extends AppCompatActivity
    implements DatePickerDialog.OnDateSetListener{
    public static final String VIEW_EXPENSE_ITEM_INTENT = "expenseItem";

    private Button btnAddExpense = null;
    DBHelper db;
    SimpleDateFormat timeFormat12Hour = new SimpleDateFormat("hh:mm aa");

    // current bg color of header
    int currentBackgroundColor = -1;

    // add_expense_header
    private ViewGroup addExpenseHeader;
    private View headerDivider;
    private Toolbar toolbar;
    private AutoResizeTextView txtExpenseInput = null; // User inputs the expense amount
    private LinearLayout layoutNumPad = null; // Layout of the numpad
    private AppCompatImageButton btnCancel = null; // exit activity without adding expense

    //add_expense_body
    private Spinner spinnerCategory = null; // Select expense category
    private Spinner spinnerPaymentMethod = null; // Select payment method
    private EditText txtNotes = null; // Add note
    private EditText txtDate = null; // Expense date. Defaults to current date. Clicking opens CalendarView
    private EditText txtTime = null; // Expense time

    //numpad
    private Button btnExpandNumPad = null;
    private ImageButton btnBackspace = null;
    private String strExpenseTotal = ""; //stores string of user inputted expense
    private Calendar calendar;

    //edit expense
    private Button btnSaveExpense;
    private Button btnDiscardExpense;
    private ExpenseItem editExpenseItem;
    private boolean isInEditMode = false;
    private volatile boolean areFieldsEdited = false;
    private Lock lock = new ReentrantLock();
    private Condition cv = lock.newCondition();

    public AddExpenseActivity() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        db = new DBHelper(this);
        calendar = Calendar.getInstance();

        addExpenseHeader = (ViewGroup) findViewById(R.id.add_expense_header);
        headerDivider = findViewById(R.id.divider);
        toolbar =(Toolbar) findViewById(R.id.add_expense_toolbar);
        btnAddExpense = (Button)findViewById(R.id.btn_add_expense_done);
        txtExpenseInput = (AutoResizeTextView) findViewById(R.id.txt_expense_total);
        btnCancel = (AppCompatImageButton)findViewById(R.id.btn_cancel);
        layoutNumPad = (LinearLayout)findViewById(R.id.add_expense_numpad);
        layoutNumPad.setVisibility(View.VISIBLE);
        btnExpandNumPad = (Button)findViewById(R.id.btn_okay);
        spinnerCategory = (Spinner)findViewById(R.id.spinner_category);
        spinnerPaymentMethod = (Spinner)findViewById(R.id.spinner_payment_method);
        txtNotes = (EditText)findViewById(R.id.add_expense_note);
        txtDate = (EditText)findViewById(R.id.add_expense_date);
        txtTime = (EditText)findViewById(R.id.add_expense_time);
        txtTime.setText(timeFormat12Hour.format(calendar.getTime()));
        btnBackspace = (ImageButton)findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strExpenseTotal.length() > 0) {
                    strExpenseTotal = strExpenseTotal.substring(0, strExpenseTotal.length() - 1);
                    if (strExpenseTotal.length() == 0){
                        setExpenseInputText("0");
                    } else {
                        setExpenseInputText(strExpenseTotal);
                    }
                }
            }
        });

        txtExpenseInput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (layoutNumPad.getVisibility() == GONE){
                    onNumPadExpand();
                } else {
                    onNumPadCollapse();
                }
            }
        });

        setExpenseInputText("0");

        btnExpandNumPad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (layoutNumPad.getVisibility() == GONE){
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

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseTime();
            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAddExpense();
            }
        });
        // populate spinners with database data
        populateSpinners();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animateBackgroundColorTransition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Initialize for expense editing
        Intent i = getIntent();
        editExpenseItem = i.getParcelableExtra(VIEW_EXPENSE_ITEM_INTENT);
        if (editExpenseItem != null){
            initializeForExpenseEditing(editExpenseItem);
        }
        setStartingColors();
    }

    private void initializeForExpenseEditing(ExpenseItem item){
        currentBackgroundColor = Color.parseColor(item.getCategory().getColour());
        isInEditMode = true;
        toolbar = (Toolbar) findViewById(R.id.add_expense_toolbar);
        toolbar.setTitle(R.string.toolbar_edit_expense_title);
        //toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_gray));
        btnCancel.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue_gray_dark));
        }

        btnAddExpense.setVisibility(View.GONE);
        findViewById(R.id.edit_expense_button_container).setVisibility(View.VISIBLE);
        btnSaveExpense = (Button) findViewById(R.id.btn_edit_expense_save);
        btnSaveExpense.setOnClickListener(onSaveButtonClicked());
        btnDiscardExpense = (Button) findViewById(R.id.btn_edit_expense_discard);
        btnDiscardExpense.setOnClickListener(onDiscardButtonClicked());

        layoutNumPad.setVisibility(View.GONE);
        btnAddExpense.setVisibility(View.GONE);
        //addExpenseHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_gray));
        headerDivider.setBackgroundColor(Color.DKGRAY);
        onNumPadCollapse();

        strExpenseTotal = Utils.formatDoubleTwoDecimalPlaces(item.getAmount());
        setExpenseInputText(strExpenseTotal);
        txtNotes.setText(item.getNote());
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.getDateMillis());
        onDateSet(null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        txtTime.setText(timeFormat12Hour.format(calendar.getTime()));
        setCategorySpinnerSelection(item.getCategory().getId());
        setPaymentMethodSpinnerSelection(item.getPaymentMethod());

        txtNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onFieldEdited();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerCategory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    onFieldEdited();
                }
                return false;
            }
        });

        spinnerPaymentMethod.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    onFieldEdited();
                }
                return false;
            }
        });

        lock.lock();
        areFieldsEdited = false;
        lock.unlock();

        btnSaveExpense.setEnabled(false);
        waitToEnableSaveButton();
    }

    @Override
    public void onBackPressed() {
        lock.lock();
        if (isInEditMode && areFieldsEdited){
            lock.unlock();
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    Utils.hideKeyboard(this);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private void setStartingColors(){
        Cursor c = (Cursor) spinnerCategory.getSelectedItem();
        int color = Color.parseColor(c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_COLOUR)));
        int colorDark = Utils.darkenColor(color);

        toolbar.setBackgroundColor(color);
        addExpenseHeader.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(colorDark);
        }
        headerDivider.setBackgroundColor(colorDark);
        currentBackgroundColor = color;
    }

    private void animateBackgroundColorTransition(){
        if (currentBackgroundColor == -1) return;
        Cursor c = (Cursor) spinnerCategory.getSelectedItem();
        String itemColor = c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_COLOUR));
        int colorTo = Color.parseColor(itemColor);

        if (currentBackgroundColor == colorTo) return;

        ValueAnimator colorAnimation = CustomAnimation.getColorTransitionAnimation(currentBackgroundColor, colorTo);
        ValueAnimator colorAnimationDark = CustomAnimation.getColorTransitionAnimation(currentBackgroundColor, Utils.darkenColor(colorTo));
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                // update background color here
                if (toolbar != null && addExpenseHeader != null && headerDivider != null) {
                    toolbar.setBackgroundColor((int) animator.getAnimatedValue());
                    addExpenseHeader.setBackgroundColor((int) animator.getAnimatedValue());
                }
            }

        });
        colorAnimationDark.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor((int) animator.getAnimatedValue());
                }
                headerDivider.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });

        colorAnimation.start();
        colorAnimationDark.start();
        currentBackgroundColor = colorTo;
    }

    private void onNumPadExpand(){
        CustomAnimation.expand(layoutNumPad);
        btnExpandNumPad.setText(R.string.okay);
        btnExpandNumPad.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
    }

    private void onNumPadCollapse(){
        CustomAnimation.collapse(layoutNumPad);
        Drawable img = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_expand_more_white_24dp);
        btnExpandNumPad.setText("");
        btnExpandNumPad.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
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
        setExpenseInputText(strExpenseTotal);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar today = Calendar.getInstance();
        String date;
        if (year == today.get(Calendar.YEAR) &&
                monthOfYear == today.get(Calendar.MONTH) &&
                dayOfMonth == today.get(Calendar.DAY_OF_MONTH)){
            date = getResources().getString(R.string.default_date);
        }else {
            date = (monthOfYear + 1) + " / " + dayOfMonth + " / " + year;
        }
        txtDate.setText(date);

        // update calendar
        calendar.set(year, monthOfYear, dayOfMonth);
        onFieldEdited();
    }

    private void setExpenseInputText(String text){
        SpannableString currentExpenses =
                new SpannableString(String.format(getResources().getString(R.string.dollar_amount), text));
        currentExpenses.setSpan(new RelativeSizeSpan(0.5f), 0, 1, 0);
        txtExpenseInput.setText(currentExpenses);
        onFieldEdited();
    }

    private double getExpenseInputAmount(){
        String text = txtExpenseInput.getText().toString();
        return Double.parseDouble(text.substring(1));
    }

    // Open CalendarView to choose date
    private void chooseDate(){
        DatePickerDialog datePicker = new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // Open TimePicker
    private void chooseTime(){
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                txtTime.setText(timeFormat12Hour.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePicker.show();
        onFieldEdited();
    }

    private ContentValues getUpdatedFieldValues(){
        double amount = getExpenseInputAmount();
        if (amount == 0){
            // expense cannot be 0.
            // Open snackbar to notify user
            Snackbar.make(findViewById(R.id.btn_add_expense_done), getString(R.string.snackbar_input_amount), Snackbar.LENGTH_SHORT)
                    .show();
            return null;
        }

        String notes = txtNotes.getText().toString();
        Cursor category = ((Cursor) spinnerCategory.getSelectedItem());
        ExpenseCategory c = ExpenseCategory.fromCursor(category);
        long categoryId = category.getInt(category.getColumnIndex(DBHelper.CategoriesTable._ID));
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();
        // Store date as time since epoch
        long date = calendar.getTimeInMillis();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.ExpensesTable.COL_AMOUNT, amount);
        cv.put(DBHelper.ExpensesTable.COL_DATE, date);
        cv.put(DBHelper.ExpensesTable.COL_CATEGORY_ID, categoryId);
        cv.put(DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID, paymentMethod);
        cv.put(DBHelper.ExpensesTable.COL_NOTES, notes);

        return cv;
    }

    // User clicks button to add expense
    private void confirmAddExpense(){
        ContentValues cv = getUpdatedFieldValues();
        if (cv != null){
            getContentResolver().insert(DBContentProvider.CONTENT_URI, cv);
            onBackPressed(); // close activity
        }
    }

    // Populate spinners with the categories and payment methods
    private void populateSpinners(){
        Cursor categoriesCursor = db.getVisibleCategories();
        Cursor pmCursor = db.getVisiblePaymentMethods();

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

    private void setPaymentMethodSpinnerSelection(String s) {
        for (int i = 0; i < spinnerPaymentMethod.getCount(); i++){
            if (spinnerPaymentMethod.getItemAtPosition(i).equals(s)){
                spinnerPaymentMethod.setSelection(i);
                break;
            }
        }
    }

    private void setCategorySpinnerSelection(int id){
        for (int i = 0; i < spinnerCategory.getCount(); i++) {
            Cursor c = (Cursor) spinnerCategory.getItemAtPosition(i);
            int itemId = c.getInt(c.getColumnIndex(DBHelper.CategoriesTable._ID));
            if (itemId == id) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void showDiscardDialog(){
        new AlertDialog.Builder(AddExpenseActivity.this)
                .setTitle(R.string.dialog_discard_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Edit expense - discard
    private View.OnClickListener onDiscardButtonClicked(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock.lock();
                if (isInEditMode && areFieldsEdited){
                    showDiscardDialog();
                    lock.unlock();
                } else {
                    lock.unlock();
                    finish();
                }
            }
        };
    }

    private View.OnClickListener onSaveButtonClicked(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = getUpdatedFieldValues();
                lock.lock();
                if (cv != null && isInEditMode && areFieldsEdited) {
                    lock.unlock();
                    final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI, editExpenseItem.getId());
                    getContentResolver()
                            .update(uri, cv, null, null);
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_saved_changes), Toast.LENGTH_SHORT)
                            .show();
                }

                finish();
            }
        };
    }

    private void onFieldEdited(){
        lock.lock();
        if (isInEditMode && !areFieldsEdited){
            areFieldsEdited = true;
            cv.signalAll();
        }
        lock.unlock();
    }

    private void waitToEnableSaveButton(){
        Thread t = new Thread(){
            @Override
            public void run() {
                lock.lock();
                try {
                    while (!areFieldsEdited){
                        try{
                            cv.await();
                        } catch(InterruptedException e) {}
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSaveExpense.setEnabled(true);
                        }
                    });
                } finally {
                    lock.unlock();
                }
            }
        };
        t.start();
    }
}
