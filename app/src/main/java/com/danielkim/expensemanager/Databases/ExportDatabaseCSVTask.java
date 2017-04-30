package com.danielkim.expensemanager.Databases;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.danielkim.expensemanager.Models.ExpenseItem;
import com.danielkim.expensemanager.MySharedPreferences;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by Daniel on 4/29/2017.
 * http://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
 */

public class ExportDatabaseCSVTask {
    public static void exportDBToCSV(Context context) {

        DBHelper dbhelper = new DBHelper(context);
        File dbFile = context.getDatabasePath(DBHelper.DATABASE_NAME);
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "expenses.csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = dbhelper.getExpenses();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                ExpenseItem item = ExpenseItem.fromCursor(curCSV, context);
                String arrStr[] = {
                        MyDateUtils.formatDateMillis(context, item.getDateMillis()),
                        String.format(context.getResources().getString(R.string.currency_amount), MySharedPreferences.getCurrency(context), Utils.formatDoubleTwoDecimalPlaces(item.getAmount())),
                        item.getCategory().getName(),
                        item.getPaymentMethod().getName(),
                        item.getNote()};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("ExportDatabaseCSVTask", sqlEx.getMessage(), sqlEx);
        }
    }
}