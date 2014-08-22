package com.janclarin.gradepath.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.janclarin.gradepath.database.DatabaseFacade;

abstract public class BaseDialog extends DialogFragment {

    public static final String DIALOG_TITLE = "Title";
    protected Context mContext;
    protected DatabaseFacade mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set mContext.
        mContext = getActivity();

        // Initialize mDatabase.
        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();
    }

    abstract public Dialog onCreateDialog(Bundle savedInstanceState);
}
