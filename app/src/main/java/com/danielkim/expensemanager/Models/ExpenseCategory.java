package com.danielkim.expensemanager.Models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.danielkim.expensemanager.Databases.DBHelper;

/**
 * Created by Daniel 657-454-7555 on 9/21/2016.
 */
public class ExpenseCategory implements Parcelable {
    private int id;
    private String name;
    private String colour;

    public ExpenseCategory(){
    }

    private ExpenseCategory(Parcel in) {
        id = in.readInt();
        name = in.readString();
        colour = in.readString();
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
    }

    public static final Creator<ExpenseCategory> CREATOR = new Creator<ExpenseCategory>() {
        @Override
        public ExpenseCategory createFromParcel(Parcel in) {
            return new ExpenseCategory(in);
        }

        @Override
        public ExpenseCategory[] newArray(int size) {
            return new ExpenseCategory[size];
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

    public int getId() {return id;}

    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }

    public static ExpenseCategory fromCursor(Cursor c) {
        ExpenseCategory category = new ExpenseCategory();
        category.setId(c.getInt(c.getColumnIndex(DBHelper.CategoriesTable._ID)));
        category.setName(c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_CATEGORY)));
        category.setColour(c.getString(c.getColumnIndex(DBHelper.CategoriesTable.COL_COLOUR)));
        return category;
    }
}
