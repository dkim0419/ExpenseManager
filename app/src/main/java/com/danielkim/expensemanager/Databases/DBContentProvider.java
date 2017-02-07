package com.danielkim.expensemanager.Databases;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;

/**
 * Created by Daniel on 7/16/2016.
 */
public class DBContentProvider extends ContentProvider {
    private DBHelper mDatabase;
    private static final int EXPENSES = 1;
    private static final int EXPENSES_ID = 2;
    private static final int EXPENSES_DATE = 3;
    private static final int EXPENSES_AMOUNT = 4;
    private static final int EXPENSES_CATEGORY = 5;
    private static final int EXPENSES_PAYMENT_METHOD = 6;
    private static final int EXPENSES_NOTES = 7;
    private static final int EXPENSES_FILTER = 8;

    private static final String AUTHORITY = "com.danielkim.expensemanager.Databases.DBContentProvider";
    private static final String BASE_PATH = "expenses";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final Uri CONTENT_URI_FILTER = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH + "/filter");

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, EXPENSES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", EXPENSES_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/date", EXPENSES_DATE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/amount", EXPENSES_AMOUNT);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/category_id", EXPENSES_CATEGORY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/payment_method_id", EXPENSES_PAYMENT_METHOD);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#/notes", EXPENSES_NOTES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/filter", EXPENSES_FILTER);
    }

    @Override
    public boolean onCreate() {
        mDatabase = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME);

        String joins = " t1 INNER JOIN " + DBHelper.CategoriesTable.TABLE_CATEGORIES_NAME +
                " t2 ON t2._id = t1." + DBHelper.ExpensesTable.COL_CATEGORY_ID;

        String groupBy = null;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case EXPENSES:
                queryBuilder.setTables(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME + joins);
                break;
            case EXPENSES_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable._ID + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_AMOUNT:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable.COL_AMOUNT + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_CATEGORY:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable.COL_CATEGORY_ID + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_DATE:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable.COL_DATE + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_NOTES:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable.COL_NOTES + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_PAYMENT_METHOD:
                // adding the ID to the original query
                queryBuilder.appendWhere(DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID + "="
                        + uri.getLastPathSegment());
                break;
            case EXPENSES_FILTER:
                groupBy = "strftime('" + MyDateUtils.MONTH_YEAR_FORMAT_SQL + "'," + DBHelper.ExpensesTable.COL_DATE + "/1000,'unixepoch')";
                queryBuilder.setTables(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, null, sortOrder, null);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case EXPENSES:
                id = sqlDB.insert(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case EXPENSES:
                rowsDeleted = sqlDB.delete(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME, selection,
                        selectionArgs);
                break;
            case EXPENSES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(
                            DBHelper.ExpensesTable.TABLE_EXPENSES_NAME,
                            DBHelper.ExpensesTable._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(
                            DBHelper.ExpensesTable.TABLE_EXPENSES_NAME,
                            DBHelper.ExpensesTable._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case EXPENSES:
                rowsUpdated = sqlDB.update(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case EXPENSES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME,
                            values,
                            DBHelper.ExpensesTable._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(DBHelper.ExpensesTable.TABLE_EXPENSES_NAME,
                            values,
                            DBHelper.ExpensesTable._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
