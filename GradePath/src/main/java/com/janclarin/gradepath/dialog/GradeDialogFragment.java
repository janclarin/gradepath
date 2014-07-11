package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.util.Collections;
import java.util.List;

/**
 * Dialog for adding and updating grades.
 */
public class GradeDialogFragment extends DialogFragment {

    /**
     * Dialog title bundle parameter to access title.
     */
    public static final String DIALOG_TITLE = "Title";
    private static final String LOG_TAG = GradeDialogFragment.class.getSimpleName();

    private DialogGradeCallbacks mListener;

    private Context mContext;
    private DatabaseFacade mDatabase;
    private Grade mGradeToUpdate;
    private boolean mOpenedFromCourse;

    private Spinner mCourseSpinner;
    private Spinner mComponentSpinner;
    private EditText mGradeName;
    private EditText mGradeReceived;
    private EditText mGradePossible;

    public GradeDialogFragment() {
    }

    /**
     * Creates a new instance of this dialog fragment.
     *
     * @param title The title of the dialog (New/Edit Grade).
     * @return A new instance of fragment NewGradeDialog.
     */
    public static GradeDialogFragment newInstance(String title) {
        GradeDialogFragment fragment = new GradeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Call when adding a new grade from course fragment.
     *
     * @param title
     * @param course
     * @return
     */
    public static GradeDialogFragment newInstance(String title, Course course) {
        GradeDialogFragment fragment = new GradeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.COURSE_KEY, course);
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

        // Set boolean to indicate it was opened from course fragment and not home.
        mOpenedFromCourse = getArguments().containsKey(MainActivity.COURSE_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_grade, null);

        // Find views.
        mCourseSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_grade_courses);
        mComponentSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_grade_components);
        mGradeName = (EditText) rootView.findViewById(R.id.et_grade_name);
        mGradeReceived = (EditText) rootView.findViewById(R.id.et_grade_received);
        mGradePossible = (EditText) rootView.findViewById(R.id.et_grade_possible);

        // Get course object from arguments.
        Course course = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);

        // Get all recent list_course.
        List<Course> courses = mDatabase.getCurrentCourses();

        Collections.sort(courses);

        // Set up course spinner mAdapter.
        ArrayAdapter<Course> adapter = new ArrayAdapter<Course>(mContext,
                android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCourseSpinner.setAdapter(adapter);

        // Select course from course fragment if adding grade from a course fragment.
        if (mOpenedFromCourse) mCourseSpinner.setSelection(adapter.getPosition(course));

        // Set on item selected listener to change categories based on course selected.
        mCourseSpinner.setOnItemSelectedListener(new CourseSelectedListener());

        // Set positive button to "Update" if updating, "Save" if not.
        String positiveButton = (mGradeToUpdate != null) ? mContext.getString(R.string.dialog_update) : mContext.getString(R.string.dialog_save);

        // Return new dialog using builder. Gets title from arguments.
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
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        // Show dialog to allow positive button to be found.
        alertDialog.show();

        // Set alert dialog's positive button to check for proper edit text fields.
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast message to display at end if need be.
                String toastMessage = "";

                // Grade name and grade from edit text fields.
                String gradeName = mGradeName.getText().toString().trim();
                String gradeReceived = mGradeReceived.getText().toString().trim();
                String gradePossible = mGradePossible.getText().toString().trim();

                // Check if grade name is missing.
                if (gradeName.isEmpty()) {
                    toastMessage = mContext.getString(R.string.grade_name_missing);

                    // Check if grade received is missing.
                } else if (gradeReceived.isEmpty()) {
                    toastMessage = mContext.getString(R.string.grade_received_missing);

                    // Check if grade possible is missing.
                } else if (gradePossible.isEmpty()) {
                    toastMessage = mContext.getString(R.string.grade_possible_missing);

                } else {
                    long courseId = ((Course) mCourseSpinner.getSelectedItem()).getId();
                    long componentId = ((GradeComponent) mComponentSpinner.getSelectedItem()).getId();

                    // Insert grade if all the other tests pass.
                    mDatabase.insertGrade(courseId, componentId, gradeName,
                            Double.parseDouble(gradeReceived), Double.parseDouble(gradePossible));

                    // Notify listeners that a grade was saved. true indicating it's a new grade.
                    if (mListener != null) mListener.onGradeSaved(mGradeToUpdate == null);

                    // Dismiss dialog.
                    alertDialog.dismiss();
                }

                // Display toast if the message is not empty.
                if (!toastMessage.isEmpty()) {
                    Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogGradeCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogGradeCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface for listeners.
     */
    public static interface DialogGradeCallbacks {

        /**
         * Called when a grade is saved.
         */
        public void onGradeSaved(boolean isNew);

    }

    /**
     * On item selected, get selected course's grade components and set category spinner.
     */
    private final class CourseSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Course selectedCourse = (Course) parent.getSelectedItem();
            List<GradeComponent> gradeComponents = mDatabase.getGradeComponents(selectedCourse.getId());

            ArrayAdapter<GradeComponent> adapter = new ArrayAdapter<GradeComponent>(mContext,
                    android.R.layout.simple_spinner_item, gradeComponents);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mComponentSpinner.setAdapter(adapter);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
