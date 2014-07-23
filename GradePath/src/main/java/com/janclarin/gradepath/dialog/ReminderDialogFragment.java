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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Dialog for adding and updating tasks.
 */
public class ReminderDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    static final String DIALOG_TITLE = "Title";

    private static final String LOG_TAG = ReminderDialogFragment.class.getSimpleName();

    /**
     * Date formatter for the due date.
     */
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E, MMMM d, y");

    private OnDialogTaskListener mListener;

    private Context mContext;

    private DatabaseFacade mDatabase;

    private Reminder mReminderToUpdate;

    private boolean mOpenedFromCourse;

    private Spinner mCourseSpinner;

    private EditText mTaskName;

    private CheckBox mCheckBoxGraded;

    private Button mDueDateButton;

    // Calendar to keep track of due date.
    private Calendar mDueDateCalendar;

    public ReminderDialogFragment() {
    }

    /**
     * Creates a new instance of this dialog fragment.
     *
     * @param title
     * @return
     */
    public static ReminderDialogFragment newInstance(String title) {
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReminderDialogFragment newInstance(String title, Course course) {
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReminderDialogFragment newInstance(String title, Reminder reminder) {
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.TASK_KEY, reminder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set mContext.
        mContext = getActivity();

        // Initialize mDatabase.
        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();

        // Calendar instance.
        mDueDateCalendar = Calendar.getInstance();

        // Set boolean to indicate it was opened from course fragment and not home.
        mOpenedFromCourse = getArguments().containsKey(MainActivity.COURSE_KEY);

        // Get task to update if it exists.
        mReminderToUpdate = (Reminder) getArguments().getSerializable(MainActivity.TASK_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_reminder, null);

        // Find views.
        mCourseSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_reminder_courses);
        mTaskName = (EditText) rootView.findViewById(R.id.et_reminder_name);
        mCheckBoxGraded = (CheckBox) rootView.findViewById(R.id.cb_graded);
        mDueDateButton = (Button) rootView.findViewById(R.id.btn_dialog_date);

        // Get all recent courses. Set up course spinner.
        List<Course> courses = mDatabase.getCurrentCourses();
        Collections.sort(courses);
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<Course>(mContext,
                android.R.layout.simple_spinner_item, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCourseSpinner.setAdapter(courseAdapter);

        // Select course from course fragment if adding grade from a course fragment.
        if (mOpenedFromCourse) {
            // Get course object from arguments.
            Course course = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
            mCourseSpinner.setSelection(courseAdapter.getPosition(course));
        }

        // If updating a task.
        if (mReminderToUpdate != null) {
            mTaskName.setText(mReminderToUpdate.getName());

            // Set course to task's course.
            Course taskCourse = new Course();
            taskCourse.setId(mReminderToUpdate.getCourseId());
            mCourseSpinner.setSelection(courseAdapter.getPosition(taskCourse));

            // Set due date calendar to task's due date.
            mDueDateCalendar = mReminderToUpdate.getDate();

            // Set graded checkbox.
            mCheckBoxGraded.setChecked(mReminderToUpdate.isGraded());
        }

        // Date picker dialog instance.
        final DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                DatePickerDialog.THEME_HOLO_LIGHT, ReminderDialogFragment.this,
                mDueDateCalendar.get(Calendar.YEAR), mDueDateCalendar.get(Calendar.MONTH),
                mDueDateCalendar.get(Calendar.DAY_OF_MONTH));

        // Set bottom padding of calendar view to 32dp.
        datePickerDialog.getDatePicker().getCalendarView().setPadding(0, 0, 0, 32);

        // Set date picker buttons.
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE,
                mContext.getString(R.string.btn_date_picker_positive),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismissing dialog calls onStop method, which notifies date set.
                        dialog.dismiss();
                    }
                }
        );
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL,
                mContext.getString(R.string.btn_date_picker_neutral),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update to due date to the day after.
                        final Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        ((DatePickerDialog) dialog).updateDate(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        dialog.dismiss();
                    }
                }
        );
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE,
                mContext.getString(R.string.btn_date_picker_negative),
                new DialogInterface.OnClickListener() {
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

        // Hide spinners and show calendar view.
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePicker.setSpinnersShown(false);
        datePicker.setCalendarViewShown(true);
        datePicker.getCalendarView().setShowWeekNumber(false);

        // Set button to date.
        mDueDateButton.setText(DATE_FORMAT.format(mDueDateCalendar.getTime()));

        // Set button click listener to show date picker dialog.
        mDueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the date.
                datePickerDialog.updateDate(
                        mDueDateCalendar.get(Calendar.YEAR), mDueDateCalendar.get(Calendar.MONTH),
                        mDueDateCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        // Set positive button to "Update" if updating, "Save" if not.
        String positiveButton = (mReminderToUpdate != null) ? mContext.getString(R.string.dialog_update) : mContext.getString(R.string.dialog_save);

        // Return new dialog. Gets title from arguments.
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setView(rootView)
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Overridden after. Prevents dialog from being dismissed without checks.
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new CancelOnClickListener())
                .create();

        // Show dialog to allow positive button to be found.
        alertDialog.show();

        // Set positive button to check for proper task information.
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTaskName.getText().toString().trim();
                if (!name.isEmpty()) {
                    long courseId = ((Course) mCourseSpinner.getSelectedItem()).getId();
                    boolean isGraded = mCheckBoxGraded.isChecked();

                    if (mReminderToUpdate == null) {
                        // Insert task into mDatabase. false because it isn't completed yet.
                        mDatabase.insertReminder(courseId, name, isGraded, false, mDueDateCalendar);
                        if (mListener != null) mListener.onReminderSaved(true);
                    } else {
                        // Update task.
                        mDatabase.updateReminder(mReminderToUpdate.getId(), courseId, name, isGraded,
                                mReminderToUpdate.isCompleted(), mDueDateCalendar);
                        if (mListener != null) mListener.onReminderSaved(false);
                    }

                    // Notify listeners that a task was saved.
                    alertDialog.dismiss();
                } else {
                    // Display warning message. No name.
                    Toast.makeText(mContext, mContext.getString(R.string.title_missing),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogTaskListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogTaskCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // Update the due date calendar object.
        mDueDateCalendar.set(Calendar.YEAR, year);
        mDueDateCalendar.set(Calendar.MONTH, monthOfYear);
        mDueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // Update the due date button text.
        mDueDateButton.setText(DATE_FORMAT.format(mDueDateCalendar.getTime()));
    }

    /**
     *
     */
    public interface OnDialogTaskListener {

        /**
         * Called when a grade is saved.
         */
        public void onReminderSaved(boolean isNew);
    }

    /**
     * Cancel button action.
     */
    private final class CancelOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int id) {
            // Dismiss dialog.
            dialog.dismiss();
        }
    }
}
