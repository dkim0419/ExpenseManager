package com.danielkim.expensemanager.Dialogs;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import com.danielkim.expensemanager.Activities.ViewCategoryPMActivity;
import com.danielkim.expensemanager.Databases.DBContentProvider;
import com.danielkim.expensemanager.Databases.DBHelper;
import com.danielkim.expensemanager.Models.ExpenseCategory;
import com.danielkim.expensemanager.Models.ExpensePaymentMethod;
import com.danielkim.expensemanager.R;
import com.danielkim.expensemanager.Utils.Utils;

import petrov.kristiyan.colorpicker.ColorPicker;

import static com.danielkim.expensemanager.Activities.ViewCategoryPMActivity.VIEW_TYPE;

/**
 * Created by Daniel on 4/24/2017.
 */

public class AddEditCategoryPMDialog extends android.support.v4.app.DialogFragment {
    private static final String EDIT_CATEGORY_KEY = "add_edit_dialog_edit_category";
    private static final String EDIT_PM_KEY = "add_edit_dialog_edit_pm";

    private ImageView mCircle;
    private EditText mName;
    private ColorPicker mColorPicker;
    private int mChosenColor;

    private boolean isInEditMode = false;
    private boolean isShowingColorPicker = false;
    private ViewCategoryPMActivity.ViewType mViewType;
    private ExpenseCategory mCategory;
    private ExpensePaymentMethod mPaymentMethod;

    public static AddEditCategoryPMDialog newInstance(ExpenseCategory category, ExpensePaymentMethod pm) {
        AddEditCategoryPMDialog dialog = new AddEditCategoryPMDialog();
        Bundle args = new Bundle();
        if (category != null) args.putParcelable(EDIT_CATEGORY_KEY, category);
        else if (pm != null) args.putParcelable(EDIT_PM_KEY, pm);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof ViewCategoryPMActivity) {
            mViewType = ((ViewCategoryPMActivity) getActivity()).getViewType();
        }
        Bundle bundle = this.getArguments();
        mCategory = bundle.getParcelable(EDIT_CATEGORY_KEY);
        mPaymentMethod = bundle.getParcelable(EDIT_PM_KEY);
        if (mCategory != null || mPaymentMethod != null) {
            isInEditMode = true;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_edit_category_pm, null);

        mChosenColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        mCircle = (ImageView) view.findViewById(R.id.circle);
        mName = (EditText) view.findViewById(R.id.edit_category_pm_name_text);

        if (mCategory != null) {
            mChosenColor = Color.parseColor(mCategory.getColour());
            mName.setText(mCategory.getName());
        } else if (mPaymentMethod != null) {
            mChosenColor = Color.parseColor(mPaymentMethod.getColour());
            mName.setText(mPaymentMethod.getName());
        }

        mCircle.setColorFilter(mChosenColor);

        mCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        builder.setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() instanceof ViewCategoryPMActivity) {
                    ContentValues cv = new ContentValues();
                    if (mViewType == ViewCategoryPMActivity.ViewType.Category) {
                        cv.put(DBHelper.CategoriesTable.COL_CATEGORY, mName.getText().toString());
                        cv.put(DBHelper.CategoriesTable.COL_COLOUR, Utils.getColorHex(mChosenColor));
                        cv.put(DBHelper.CategoriesTable.COL_VISIBLE, 1);
                        if (isInEditMode) {
                            final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI_CATEGORIES, mCategory.getId());
                            getActivity().getContentResolver().update(uri, cv, null, null);
                        } else {
                            getActivity().getContentResolver().insert(DBContentProvider.CONTENT_URI_CATEGORIES, cv);
                        }
                    } else {
                        cv.put(DBHelper.PaymentMethodsTable.COL_PAYMENT_METHOD, mName.getText().toString());
                        cv.put(DBHelper.PaymentMethodsTable.COL_COLOUR, Utils.getColorHex(mChosenColor));
                        cv.put(DBHelper.PaymentMethodsTable.COL_VISIBLE, 1);
                        if (isInEditMode) {
                            final Uri uri = ContentUris.withAppendedId(DBContentProvider.CONTENT_URI_PAYMENT_METHODS, mPaymentMethod.getId());
                            getActivity().getContentResolver().update(uri, cv, null, null);
                        } else {
                            getActivity().getContentResolver().insert(DBContentProvider.CONTENT_URI_PAYMENT_METHODS, cv);
                        }
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        int titleResId;
        if (isInEditMode) {
            if (mViewType == ViewCategoryPMActivity.ViewType.Category) titleResId = R.string.dialog_edit_category_title;
            else titleResId = R.string.dialog_edit_pm_title;
        } else {
            if (mViewType == ViewCategoryPMActivity.ViewType.Category) titleResId = R.string.dialog_add_category_title;
            else titleResId = R.string.dialog_add_pm_title;
        }

        builder.setTitle(titleResId);

        builder.setView(view);
        return builder.create();
    }

    private void showColorPicker() {
        if (isShowingColorPicker) return;

        isShowingColorPicker = true;
        mColorPicker = new ColorPicker(getActivity());
        mColorPicker.setDefaultColorButton(mChosenColor);
        mColorPicker.setRoundColorButton(true);
        mColorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                mCircle.setColorFilter(color);
                mChosenColor = color;
                isShowingColorPicker = false;
            }

            @Override
            public void onCancel() {
                isShowingColorPicker = false;
            }
        });

        mColorPicker.show();
    }
}
