package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SemesterDialogFragment extends BaseDialogFragment {

    private OnDialogSemesterListener mListener;

    private Semester mSemesterToUpdate;
    private Spinner mSeasonSpinner;
    private Spinner mYearSpinner;
    private CheckBox mCurrentCheckBox;
    private EditText mGPA;

    public SemesterDialogFragment() {
        // Required empty public constructor
    }

    public static SemesterDialogFragment newInstance(String title) {
        SemesterDialogFragment fragment = new SemesterDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public static SemesterDialogFragment newInstance(String title, Semester semester) {
        SemesterDialogFragment fragment = new SemesterDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.SEMESTER_KEY, semester);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get semester to update if it's available.
        mSemesterToUpdate = (Semester) getArguments().getSerializable(MainActivity.SEMESTER_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_semester, null);

        // Find views.
        mSeasonSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_semester_season);
        mYearSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_semester_year);
        mCurrentCheckBox = (CheckBox) rootView.findViewById(R.id.cb_dialog_semester_current);
        mGPA = (EditText) rootView.findViewById(R.id.et_dialog_semester_gpa);

        // If the semester is current, hide the GPA, show otherwise.
        mCurrentCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int visibility = isChecked ? View.GONE : View.VISIBLE;
                mGPA.setVisibility(visibility);
            }
        });

        // Set to current by default if there are no semesters.
        mCurrentCheckBox.setChecked(mDatabase.noSemesters());

        // Create array mAdapter for seasons and set mAdapter.
        ArrayAdapter<Semester.Season> seasonAdapter = new ArrayAdapter<Semester.Season>(
                mContext, android.R.layout.simple_spinner_item, Semester.seasons);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSeasonSpinner.setAdapter(seasonAdapter);

        // Get range of years to display.
        List<Integer> years = new ArrayList<Integer>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 8; i < currentYear + 4; i++) years.add(i);

        // Create array mAdapter for years and set mAdapter.
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<Integer>(mContext,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yearAdapter);

        // Set spinners to semester values if it exists.
        if (mSemesterToUpdate != null) {
            mSeasonSpinner.setSelection(seasonAdapter.getPosition(mSemesterToUpdate.getSeasonEnum()));
            mYearSpinner.setSelection(yearAdapter.getPosition(mSemesterToUpdate.getYear()));
        } else {
            // Set default selection to current year.
            mYearSpinner.setSelection(yearAdapter.getPosition(currentYear));
        }

        final String positiveButton;
        // Set positive button to "Update" if updating, "Save" if not.
        // Set checkbox properly based on semester's status.
        if (mSemesterToUpdate != null) {
            mCurrentCheckBox.setChecked(mSemesterToUpdate.isCurrent());
            positiveButton = mContext.getString(R.string.dialog_update);
        } else {
            positiveButton = mContext.getString(R.string.dialog_save);
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setView(rootView)
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Overridden after. Prevents dialog from being dismissed with check.
                    }
                })
                .setNegativeButton(mContext.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss dialog.
                        dialog.dismiss();
                    }
                })
                .create();

        // Show dialog to allow positive button to be found.
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String season = (mSeasonSpinner.getSelectedItem()).toString();
                int year = (Integer) mYearSpinner.getSelectedItem();

                // Get gpa if it the edit text field isn't empty.
                String gpaString = mGPA.getText().toString().trim();
                double gpa = gpaString.isEmpty() ? -1 : Double.parseDouble(gpaString);

                boolean isCurrent = mCurrentCheckBox.isChecked();

                Semester semester;
                // Insert semester if it's not being updated.
                if (mSemesterToUpdate == null) {
                    semester = mDatabase.insertSemester(
                            season,
                            year,
                            gpa,
                            isCurrent
                    );
                } else {
                    // Update the semester.
                    mDatabase.updateSemester(
                            mSemesterToUpdate.getId(),
                            season,
                            year,
                            gpa,
                            isCurrent
                    );
                    semester = null;
                }

                // Notify listener that semester is saved.
                if (mListener != null) {
                    mListener.onSemesterSaved(mSemesterToUpdate == null, semester);
                }
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogSemesterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogSemesterCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Listener interface.
     */
    public interface OnDialogSemesterListener {

        /**
         * Called when a semester is saved.
         */
        public void onSemesterSaved(boolean isNew, Semester semester);
    }

}
