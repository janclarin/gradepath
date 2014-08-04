package com.janclarin.gradepath.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.janclarin.gradepath.database.DatabaseFacade;

abstract public class BaseFragment extends Fragment {

    protected Context mContext;
    protected DatabaseFacade mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();

        setHasOptionsMenu(true);
    }
}
