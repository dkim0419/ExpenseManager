package com.danielkim.expensemanager.Models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.danielkim.expensemanager.Databases.DBHelper;

/**
 * Created by Daniel on 4/25/2017.
 */

public class ExpensePaymentMethod implements Parcelable {
    private int id;
    private String name;
    private String colour;
    private boolean isVisible;

    public ExpensePaymentMethod(){
    }

    private ExpensePaymentMethod(Parcel in) {
        id = in.readInt();
        name = in.readString();
        colour = in.readString();
        isVisible = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(colour);
        dest.writeByte((byte)(isVisible ? 1 : 0));
    }

    public static final Creator<ExpensePaymentMethod> CREATOR = new Creator<ExpensePaymentMethod>() {
        @Override
        public ExpensePaymentMethod createFromParcel(Parcel in) {
            return new ExpensePaymentMethod(in);
        }

        @Override
        public ExpensePaymentMethod[] newArray(int size) {
            return new ExpensePaymentMethod[size];
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setIsVisible(boolean visible) {
        this.isVisible = visible;
    }

    public int getId() {return id;}

    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public static ExpensePaymentMethod fromCursor(Cursor c) {
        ExpensePaymentMethod pm = new ExpensePaymentMethod();
        pm.setId(c.getInt(c.getColumnIndex(DBHelper.PaymentMethodsTable._ID)));
        pm.setName(c.getString(c.getColumnIndex(DBHelper.PaymentMethodsTable.COL_PAYMENT_METHOD)));
        pm.setColour(c.getString(c.getColumnIndex(DBHelper.PaymentMethodsTable.COL_COLOUR)));
        pm.setIsVisible(c.getInt(c.getColumnIndex(DBHelper.PaymentMethodsTable.COL_VISIBLE)) != 0);
        return pm;
    }
}
