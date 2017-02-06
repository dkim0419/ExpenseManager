package com.danielkim.expensemanager.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.Models.ExpenseItem;

/**
 * Created by Daniel on 2/5/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;

    // Used to insert default values into table. Prevent recursive call to getDatabase()
    private boolean isCreating = false;
    private SQLiteDatabase currentDb = null;

    //Databases for tracking expenses, categories, payment methods
    public static abstract class ExpensesTable implements BaseColumns {
        public static final String TABLE_EXPENSES_NAME = "expenses";
        public static final String COL_DATE = "date";
        public static final String COL_AMOUNT = "amount";
        public static final String COL_CATEGORY_ID = "category_id";
        //TODO: MAKE THIS INTO AN ACTUAL ID (currently is just the payment method as string)
        public static final String COL_PAYMENT_METHOD_ID = "payment_method_id";
        public static final String COL_NOTES = "notes";
    }

    public static abstract class CategoriesTable implements BaseColumns{
        public static final String TABLE_CATEGORIES_NAME = "categories";
        public static final String COL_CATEGORY = "category";
        public static final String COL_COLOUR = "colour";
        public static final String COL_DRAWABLE = "icon";
    }

    public static abstract class PaymentMethodsTable implements BaseColumns{
        public static final String TABLE_PAYMENT_METHODS_NAME = "payment_methods";
        public static final String COL_PAYMENT_METHOD = "payment_method";
        public static final String COL_COLOUR = "colour";
    }

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_EXPENSES_TB =
            "CREATE TABLE " + ExpensesTable.TABLE_EXPENSES_NAME + " (" +
                    ExpensesTable._ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    ExpensesTable.COL_DATE + " INTEGER " + COMMA_SEP +
                    ExpensesTable.COL_AMOUNT + " DOUBLE " + COMMA_SEP +
                    ExpensesTable.COL_CATEGORY_ID + " INTEGER " + COMMA_SEP +
                    ExpensesTable.COL_PAYMENT_METHOD_ID + " TEXT " + COMMA_SEP +
                    ExpensesTable.COL_NOTES + " TEXT " + COMMA_SEP +
                    "FOREIGN KEY(" + ExpensesTable.COL_CATEGORY_ID + ") REFERENCES " + CategoriesTable.TABLE_CATEGORIES_NAME + "(" +  CategoriesTable._ID + ")" +
                    ")";

    private static final String SQL_CREATE_CATEGORIES_TB =
            "CREATE TABLE " + CategoriesTable.TABLE_CATEGORIES_NAME + " (" +
                    CategoriesTable._ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    CategoriesTable.COL_CATEGORY + " TEXT " + COMMA_SEP +
                    CategoriesTable.COL_COLOUR + " TEXT " + COMMA_SEP +
                    CategoriesTable.COL_DRAWABLE + " TEXT " + ")";

    private static final String SQL_CREATE_PAYMENT_METHODS_TB =
            "CREATE TABLE " + PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME + " (" +
                    PaymentMethodsTable._ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                    PaymentMethodsTable.COL_PAYMENT_METHOD + " TEXT " + COMMA_SEP +
                    PaymentMethodsTable.COL_COLOUR + " TEXT " + ")";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static void createIndex(SQLiteDatabase db, String column, String table) {
        db.execSQL("CREATE INDEX " + column + "_idx ON " + table + "(" + column + ");");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EXPENSES_TB);
        db.execSQL(SQL_CREATE_CATEGORIES_TB);
        db.execSQL(SQL_CREATE_PAYMENT_METHODS_TB);

        currentDb = db;
        isCreating = true;

        // Add index for tables
        // Expense Table
        createIndex(db, ExpensesTable.COL_AMOUNT, ExpensesTable.TABLE_EXPENSES_NAME);
        createIndex(db, ExpensesTable.COL_CATEGORY_ID, ExpensesTable.TABLE_EXPENSES_NAME);
        createIndex(db, ExpensesTable.COL_DATE, ExpensesTable.TABLE_EXPENSES_NAME);
        createIndex(db, ExpensesTable.COL_NOTES, ExpensesTable.TABLE_EXPENSES_NAME);
        createIndex(db, ExpensesTable.COL_PAYMENT_METHOD_ID, ExpensesTable.TABLE_EXPENSES_NAME);
        /*
        // Categories table
        createIndex(db, CategoriesTable.COL_CATEGORY, CategoriesTable.TABLE_CATEGORIES_NAME);
        createIndex(db, CategoriesTable.COL_COLOUR, CategoriesTable.TABLE_CATEGORIES_NAME);
        createIndex(db, CategoriesTable.COL_DRAWABLE, CategoriesTable.TABLE_CATEGORIES_NAME);

        // Payment Methods table
        createIndex(db, PaymentMethodsTable.COL_COLOUR, PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME);
        createIndex(db, PaymentMethodsTable.COL_PAYMENT_METHOD, PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME);*/

        // Add default entries into the tables
        setDefaultCategories();
        setDefaultPaymentMethods();
        currentDb = null;
        isCreating = false;
    }

    private void setDefaultCategories(){
        insertNewCategory("Food", "#8bc34a", "drawable");
        insertNewCategory("Leisure", "#f44336", "drawable");
        insertNewCategory("School", "#00bcd4", "drawable");
        insertNewCategory("Groceries", "#e91e63", "drawable");
        insertNewCategory("Transportation", "#ffc107", "drawable");
        insertNewCategory("Utilities", "#3f51b5", "drawable");
        insertNewCategory("Other", "#9e9e9e", "drawable");
    }

    private void setDefaultPaymentMethods(){
        insertNewPaymentMethod("Debit", "#4caf50");
        insertNewPaymentMethod("Credit", "#f44336");
        insertNewPaymentMethod("Cash", "#ff9800");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ExpensesTable.TABLE_EXPENSES_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_CATEGORIES_NAME);
        onCreate(db);
    }

    //insert new transaction into the Expenses table
    public long insertNewExpense(long date, double amt, long categoryId, String paymentMethod, String notes){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ExpensesTable.COL_DATE, date);
        cv.put(ExpensesTable.COL_AMOUNT, amt);
        cv.put(ExpensesTable.COL_CATEGORY_ID, categoryId);
        cv.put(ExpensesTable.COL_PAYMENT_METHOD_ID, paymentMethod);
        cv.put(ExpensesTable.COL_NOTES, notes);

        return db.insert(ExpensesTable.TABLE_EXPENSES_NAME, null, cv);
    }

    // returns the rows inserted between date1 and date2
    public Cursor getExpenseTableDataForMonth(int month, int year){
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery("SELECT * FROM " +
                ExpensesTable.TABLE_EXPENSES_NAME + " WHERE " +
                "strftime('%m/%Y', " + ExpensesTable.COL_DATE + ") = "
                + month + "/" + year, null);
    }

    public ExpenseItem getItemAt(int position){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ExpensesTable.COL_AMOUNT,
                ExpensesTable.COL_DATE,
                ExpensesTable.COL_CATEGORY_ID,
                ExpensesTable.COL_NOTES,
                ExpensesTable.COL_PAYMENT_METHOD_ID
        };

        Cursor c = db.query(ExpensesTable.TABLE_EXPENSES_NAME, projection, null, null, null, null, null);

        if (c.moveToPosition(position)){
            ExpenseItem item = new ExpenseItem();
            item.setId(c.getColumnIndex(ExpensesTable._ID));
            item.setAmount(c.getDouble(c.getColumnIndex(ExpensesTable.COL_AMOUNT)));
            Cursor category = getCategoryColourFromName(c.getString(c.getColumnIndex(ExpensesTable.COL_CATEGORY_ID)));
            ExpenseCategory ec = new ExpenseCategory();
            ec.setId(category.getInt(c.getColumnIndex(CategoriesTable._ID)));
            ec.setName(category.getString(c.getColumnIndex(CategoriesTable.COL_CATEGORY)));
            ec.setColour(category.getString(c.getColumnIndex(CategoriesTable.COL_COLOUR)));
            item.setCategory(ec);
            item.setDateMillis(c.getLong(c.getColumnIndex(ExpensesTable.COL_DATE)));
            item.setNote(c.getString(c.getColumnIndex(ExpensesTable.COL_NOTES)));
            item.setPaymentMethod(c.getString(c.getColumnIndex(ExpensesTable.COL_PAYMENT_METHOD_ID)));
            c.close();
            return item;
        }

        return null;
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { ExpensesTable._ID };
        Cursor c = db.query(ExpensesTable.TABLE_EXPENSES_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public long insertNewCategory(String category, String colour, String drawable){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CategoriesTable.COL_CATEGORY, category);
        cv.put(CategoriesTable.COL_COLOUR, colour);
        cv.put(CategoriesTable.COL_DRAWABLE, drawable);

        long rowId = db.insert(CategoriesTable.TABLE_CATEGORIES_NAME, null, cv);
        return rowId;
    }

    public long insertNewPaymentMethod(String paymentMethod, String colour){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PaymentMethodsTable.COL_PAYMENT_METHOD, paymentMethod);
        cv.put(PaymentMethodsTable.COL_COLOUR, colour);

        long rowId = db.insert(PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME, null, cv);
        return rowId;
    }

    public Cursor getExpenses(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " +
                ExpensesTable.TABLE_EXPENSES_NAME, null);

        return c;
    }

    public Cursor getCategories(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " +
                CategoriesTable.TABLE_CATEGORIES_NAME, null);

        return c;
    }

    public Cursor getCategoryColourFromName(String name){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "
            + CategoriesTable.TABLE_CATEGORIES_NAME + "WHERE " + CategoriesTable.COL_CATEGORY + "=" + name, null);

        return c;
    }


    public Cursor getPaymentMethods(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " +
                PaymentMethodsTable.TABLE_PAYMENT_METHODS_NAME, null);

        return c;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if(isCreating && currentDb != null){
            return currentDb;
        }
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if(isCreating && currentDb != null){
            return currentDb;
        }
        return super.getReadableDatabase();
    }
}
