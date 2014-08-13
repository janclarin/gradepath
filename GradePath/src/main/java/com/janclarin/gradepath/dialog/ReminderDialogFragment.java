package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.service.ReminderClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Dialog for adding and updating tasks.
 */
public class ReminderDialogFragment extends BaseDialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String LOG_TAG = ReminderDialogFragment.class.getSimpleName();

    /**
     * Date formatter for the due date.
     */
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E, MMMM d, y");

    private OnDialogReminderListener mListener;
    private Reminder mReminderToUpdate;
    private boolean mOpenedFromCourse;
    private Spinner mCourseSpinner;
    private EditText mReminderName;
    private CheckBox mCheckBoxGraded;
    private Button mReminderDateButton;
    private Button mReminderTimeButton;

    // Calendar to keep track of due date.
    private Calendar mDateCalendar;
    private int mDefaultHour = 12;
    private int mDefaultMinutes = 0;

    private ReminderClient mReminderClient;

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
        args.putSerializable(MainActivity.REMINDER_KEY, reminder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calendar instance.
        mDateCalendar = Calendar.getInstance();
        mDateCalendar.set(Calendar.HOUR_OF_DAY, mDefaultHour);
        mDateCalendar.set(Calendar.MINUTE, mDefaultMinutes);
        mDateCalendar.set(Calendar.SECOND, 0);
        mDateCalendar.set(Calendar.MILLISECOND, 0);

        // Set boolean to indicate it was opened from course fragment and not home.
        mOpenedFromCourse = getArguments().containsKey(MainActivity.COURSE_KEY);

        // Get reminder to update if it exists.
        mReminderToUpdate = (Reminder) getArguments().getSerializable(MainActivity.REMINDER_KEY);

        // Create service client and bind our activity to it.
        mReminderClient = new ReminderClient(getActivity());
        mReminderClient.doBindService();
    }

    @Override
    public void onStop() {
        // Prevent activity from leaking.
        if (mReminderClient != null)
            mReminderClient.doUnbindService();
        super.onStop();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_reminder, null);

        // Find views.
        mReminderName = (EditText) rootView.findViewById(R.id.et_reminder_name);
        mCourseSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_reminder_courses);
        mCheckBoxGraded = (CheckBox) rootView.findViewById(R.id.cb_graded);
        mReminderDateButton = (Button) rootView.findViewById(R.id.btn_dialog_date);
        mReminderTimeButton = (Button) rootView.findViewById(R.id.btn_dialog_time);

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

        // If updating a reminder.
        if (mReminderToUpdate != null) {
            mReminderName.setText(mReminderToUpdate.getName());

            // Set course to reminder course.
            Course reminderCourse = new Course();
            reminderCourse.setId(mReminderToUpdate.getCourseId());
            mCourseSpinner.setSelection(courseAdapter.getPosition(reminderCourse));

            // Set due date calendar to reminder due date.
            mDateCalendar = mReminderToUpdate.getReminderDate();

            // Set graded checkbox.
            mCheckBoxGraded.setChecked(mReminderToUpdate.isExam());
        }

        /* Set up date picker dialog. */
        final DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                DatePickerDialog.THEME_HOLO_LIGHT, ReminderDialogFragment.this,
                mDateCalendar.get(Calendar.YEAR), mDateCalendar.get(Calendar.MONTH),
                mDateCalendar.get(Calendar.DAY_OF_MONTH));

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

        /* Set up time picker dialog. */
        final TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                AlertDialog.THEME_HOLO_LIGHT, ReminderDialogFragment.this,
                mDateCalendar.get(Calendar.HOUR_OF_DAY), mDateCalendar.get(Calendar.MINUTE), false);

        // Set button to date.
        mReminderDateButton.setText(DATE_FORMAT.format(mDateCalendar.getTime()));

        // Set button click listener to show date picker dialog.
        mReminderDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the date.
                datePickerDialog.updateDate(
                        mDateCalendar.get(Calendar.YEAR), mDateCalendar.get(Calendar.MONTH),
                        mDateCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        // Set button to time.
        mReminderTimeButton.setText(Reminder.TIME_FORMAT.format(mDateCalendar.getTime()));

        // Set button click listener to show time picker dialog.
        mReminderTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the time.
                timePickerDialog.updateTime(
                        mDateCalendar.get(Calendar.HOUR_OF_DAY), mDateCalendar.get(Calendar.MINUTE));
                timePickerDialog.show();
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
                String name = mReminderName.getText().toString().trim();
                if (!name.isEmpty()) {
                    long courseId = ((Course) mCourseSpinner.getSelectedItem()).getId();
                    boolean isGraded = mCheckBoxGraded.isChecked();

                    long reminderId;
                    if (mReminderToUpdate == null) {
                        // Insert task into mDatabase. false because it isn't completed yet.
                        reminderId = mDatabase.insertReminder(courseId, name, isGraded, false, mDateCalendar);
                        if (mListener != null) mListener.onReminderSaved(true);
                    } else {
                        // Update task.
                        reminderId = mReminderToUpdate.getId();
                        mDatabase.updateReminder(reminderId, courseId, name, isGraded,
                                mReminderToUpdate.isCompleted(), mDateCalendar);
                        if (mListener != null) mListener.onReminderSaved(false);
                    }

                    // Set future notification.
                    mReminderClient.setAlarmForNotification(mDatabase.getReminder(reminderId));

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
            mListener = (OnDialogReminderListener) activity;
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
        mDateCalendar.set(Calendar.YEAR, year);
        mDateCalendar.set(Calendar.MONTH, monthOfYear);
        mDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // Update the reminder date button text.
        mReminderDateButton.setText(DATE_FORMAT.format(mDateCalendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Update the reminder calendar object.
        mDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mDateCalendar.set(Calendar.MINUTE, minute);

        // Update the reminder time button text.
        mReminderTimeButton.setText(Reminder.TIME_FORMAT.format(mDateCalendar.getTime()));
    }

    /**
     *
     */
    public interface OnDialogReminderListener {

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
