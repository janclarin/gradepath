package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Semester;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SemesterDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    static final String DIALOG_TITLE = "Title";

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, y");

    private OnDialogSemesterCallbacks mListener;

    private Context mContext;

    private DatabaseFacade mDatabase;

    private DatePickerDialog mDatePickerDialog;

    private Semester mSemesterToUpdate;

    private Spinner mSeasonSpinner;

    private Spinner mYearSpinner;

    private CheckBox mCurrentCheckBox;

    private EditText mGPA;

    private TextView mLastDayHeader;

    private Button mLastDayButton;

    private Calendar mLastDayCalendar;

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

        // Set mContext.
        mContext = getActivity();

        // Get semester to update if it's available.
        mSemesterToUpdate = (Semester) getArguments().getSerializable(MainActivity.SEMESTER_KEY);

        // Initialize mDatabase.
        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_semester, null);

        // Find views.
        mSeasonSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_semester_season);
        mYearSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_semester_year);
        mCurrentCheckBox = (CheckBox) rootView.findViewById(R.id.cb_dialog_semester_current);
        mGPA = (EditText) rootView.findViewById(R.id.et_dialog_semester_gpa);
        mLastDayHeader = (TextView) rootView.findViewById(R.id.tv_dialog_semester_last_day_header);
        mLastDayButton = (Button) rootView.findViewById(R.id.btn_dialog_semester_last_day);
        // Initialize calendar.
        mLastDayCalendar = Calendar.getInstance();

        // Set on checked change listener.
        mCurrentCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGPA.setVisibility(View.GONE);
                    mLastDayHeader.setVisibility(View.VISIBLE);
                    mLastDayButton.setVisibility(View.VISIBLE);

                    // Set up date pickers if they haven't been yet.
                    if (mDatePickerDialog == null) setUpDatePickers();
                } else {
                    mGPA.setVisibility(View.VISIBLE);
                    mLastDayHeader.setVisibility(View.INVISIBLE);
                    mLastDayButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Set to current by default if there are no semesters.
        mCurrentCheckBox.setChecked(!mDatabase.noSemesters());

        // Set up spinners.
        setUpSpinners();

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
                    semester = mDatabase.insertSemester(season, year, gpa, isCurrent, mLastDayCalendar);
                } else {
                    // Update the semester.
                    mDatabase.updateSemester(mSemesterToUpdate.getId(), season, year, gpa, isCurrent,
                            mLastDayCalendar);
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

    /**
     * Sets up the date pickers and initializes the buttons.
     */
    public void setUpDatePickers() {

        mDatePickerDialog = new DatePickerDialog(mContext,
                DatePickerDialog.THEME_HOLO_LIGHT, SemesterDialogFragment.this,
                mLastDayCalendar.get(Calendar.YEAR), mLastDayCalendar.get(Calendar.MONTH),
                mLastDayCalendar.get(Calendar.DAY_OF_MONTH));

        mDatePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE,
                mContext.getString(R.string.btn_date_picker_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog, notifies date set.
                        dialog.dismiss();
                    }
                }
        );
        mDatePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE,
                mContext.getString(R.string.btn_date_picker_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set to current date, i.e. no change.
                        DatePicker datePicker = ((DatePickerDialog) dialog).getDatePicker();
                        datePicker.updateDate(datePicker.getYear(), datePicker.getMonth(),
                                datePicker.getDayOfMonth());
                        dialog.dismiss();
                    }
                }
        );

        DatePicker datePicker = mDatePickerDialog.getDatePicker();

        // Set default text for last day.
        mLastDayButton.setText(DATE_FORMAT.format(mLastDayCalendar.getTime()));
        mLastDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.updateDate(mLastDayCalendar.get(Calendar.YEAR),
                        mLastDayCalendar.get(Calendar.MONTH),
                        mLastDayCalendar.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.show();
            }
        });

    }

    public void setUpSpinners() {
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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogSemesterCallbacks) activity;
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

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // Update the date on calendar.
        mLastDayCalendar.set(Calendar.YEAR, year);
        mLastDayCalendar.set(Calendar.MONTH, monthOfYear);
        mLastDayCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // Update the button's date.
        mLastDayButton.setText(DATE_FORMAT.format(mLastDayCalendar.getTime()));
    }

    /**
     * Listener interface.
     */
    public interface OnDialogSemesterCallbacks {

        /**
         * Called when a semester is saved.
         */
        public void onSemesterSaved(boolean isNew, Semester semester);
    }

}
