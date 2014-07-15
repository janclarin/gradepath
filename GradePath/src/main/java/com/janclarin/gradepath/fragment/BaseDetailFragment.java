package com.janclarin.gradepath.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.janclarin.gradepath.database.DatabaseFacade;

abstract public class BaseDetailFragment extends Fragment {

    protected Context mContext;
    protected DatabaseFacade mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();
    }
}
