package com.danielkim.expensemanager.Models;

import android.database.Cursor;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Utils.MyDateUtils;
import com.danielkim.expensemanager.Utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Daniel on 11/7/2016.
 */

// Holds all items grouped by time period
public class ExpenseItems {
    private HashMap<Date, List<ExpenseItem>> items;
    private ExpensePeriod expensePeriod;

    public ExpenseItems(Cursor c, ExpensePeriod p){
        this.expensePeriod = p;
        getItemsFromCursor(c);
    }

    public HashMap<Date, List<ExpenseItem>> getItems() {
        return items;
    }

    public void setItems(HashMap<Date, List<ExpenseItem>> items) {
        this.items = items;
    }

    public void addItem(Date d, ExpenseItem item){
        Date date = MyDateUtils.convertDateToNearestMonthYear(d);
        List<ExpenseItem> prev = items.get(date);

        if (prev == null){
            prev = new ArrayList<>();
        }

        prev.add(item);
        items.put(d, prev);
    }

    public ExpensePeriod getExpensePeriod() {
        return expensePeriod;
    }

    private void getItemsFromCursor(Cursor c){
        items.clear();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            Date d = MyDateUtils.convertDateToNearestMonthYear(new Date(c.getColumnIndex(DBHelper.ExpensesTable.COL_DATE)));
            List<ExpenseItem> prev = items.get(d);

            ExpenseItem i = ExpenseItem.fromCursor(c);
            if (prev == null){
                prev = new ArrayList<>();
            }

            prev.add(i);
            items.put(d, prev);
        }
    }
}
