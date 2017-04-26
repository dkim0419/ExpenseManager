package com.danielkim.expensemanager.Models;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.danielkim.expensemanager.Databases.DBHelper;

/**
 * Created by Daniel on 2/17/2016.
 */
public class ExpenseItem implements Parcelable{
    private int id; // id of item in database
    private double amount; // amount of expense
    private long dateMillis; // date of expense in milliseconds since epoch time
    private ExpenseCategory category; // category of expense
    private ExpensePaymentMethod paymentMethod; // payment method of expense
    private String note; // custom note for the expense

    public ExpenseItem() {
    }

    private ExpenseItem(Parcel in){
        id = in.readInt();
        amount = in.readDouble();
        dateMillis = in.readLong();
        category = in.readParcelable(ExpenseCategory.class.getClassLoader());
        paymentMethod = in.readParcelable(ExpensePaymentMethod.class.getClassLoader());
        note = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(amount);
        dest.writeLong(dateMillis);
        dest.writeParcelable(category, flags);
        dest.writeParcelable(paymentMethod, flags);
        dest.writeString(note);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public ExpensePaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(ExpensePaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static final Parcelable.Creator<ExpenseItem> CREATOR = new Parcelable.Creator<ExpenseItem>() {
        public ExpenseItem createFromParcel(Parcel in) {
            return new ExpenseItem(in);
        }
        public ExpenseItem[] newArray(int size) {
            return new ExpenseItem[size];
        }
    };

    public static ExpenseItem fromCursor(Cursor c, Context context) {
        DBHelper db = new DBHelper(context);
        ExpenseItem item = new ExpenseItem();
        item.setId(c.getInt(c.getColumnIndex(DBHelper.ExpensesTable._ID)));
        item.setAmount(c.getDouble(c.getColumnIndex(DBHelper.ExpensesTable.COL_AMOUNT)));
        ExpenseCategory category = db.getCategoryFromId(c.getInt(c.getColumnIndex(DBHelper.ExpensesTable.COL_CATEGORY_ID)));
        item.setCategory(category);
        item.setNote(c.getString(c.getColumnIndex(DBHelper.ExpensesTable.COL_NOTES)));
        ExpensePaymentMethod pm = db.getPaymentMethodFromId(c.getInt(c.getColumnIndex(DBHelper.ExpensesTable.COL_PAYMENT_METHOD_ID)));
        item.setPaymentMethod(pm);
        item.setDateMillis(c.getLong(c.getColumnIndex(DBHelper.ExpensesTable.COL_DATE)));
        return item;
    }
}
