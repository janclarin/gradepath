package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.util.Collections;
import java.util.List;

/**
 * Dialog for adding and updating grades.
 */
public class GradeDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = GradeDialogFragment.class.getSimpleName();

    private OnDialogGradeListener mListener;

    private Grade mGradeToUpdate;
    private boolean mOpenedFromCourse;
    private Course mCourseSelected;
    private ArrayAdapter<GradeComponent> mComponentAdapter;

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
     * @return A new instance of GradeDialogFragment.
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
     * @return A new instance of GradeDialogFragment.
     */
    public static GradeDialogFragment newInstance(String title, Course course) {
        GradeDialogFragment fragment = new GradeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Call when updating a grade.
     */
    public static GradeDialogFragment newInstance(String title, Grade grade) {
        GradeDialogFragment fragment = new GradeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(MainActivity.GRADE_KEY, grade);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set boolean to indicate it was opened from course fragment and not home.
        mOpenedFromCourse = getArguments().containsKey(MainActivity.COURSE_KEY);

        mGradeToUpdate = (Grade) getArguments().getSerializable(MainActivity.GRADE_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_grade, null);

        // Find views.
        mCourseSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_grade_courses);
        mComponentSpinner = (Spinner) rootView.findViewById(R.id.spn_dialog_grade_components);
        mGradeName = (EditText) rootView.findViewById(R.id.et_grade_name);
        mGradeReceived = (EditText) rootView.findViewById(R.id.et_grade_received);
        mGradePossible = (EditText) rootView.findViewById(R.id.et_grade_possible);

        // Get all recent list_course.
        List<Course> courses = mDatabase.getCurrentCourses();
        Collections.sort(courses);

        // Set up course spinner mAdapter.
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<Course>(mContext,
                android.R.layout.simple_spinner_item, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCourseSpinner.setAdapter(courseAdapter);

        // Set on item selected listener to change categories based on course selected.
        mCourseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mGradeToUpdate == null) {
                    mCourseSelected = (Course) parent.getSelectedItem();
                    List<GradeComponent> gradeComponents = mDatabase.getGradeComponents(mCourseSelected.getId());

                    mComponentAdapter = new ArrayAdapter<GradeComponent>(mContext,
                            android.R.layout.simple_spinner_item, gradeComponents);
                    mComponentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mComponentSpinner.setAdapter(mComponentAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Select course from course fragment if adding grade from a course fragment.
        if (mOpenedFromCourse) {
            // Get course object from arguments.
            Course course = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
            mCourseSpinner.setSelection(courseAdapter.getPosition(course));
        }

        // Display grade's previous fields if updating.
        if (mGradeToUpdate != null) {
            Course course = mDatabase.getCourse(mGradeToUpdate.getCourseId());
            GradeComponent gradeComponent = mDatabase.getGradeComponent(mGradeToUpdate.getComponentId());

            mGradeName.setText(mGradeToUpdate.getName());
            mGradeReceived.setText(Double.toString(mGradeToUpdate.getPointsReceived()));
            mGradePossible.setText(Double.toString(mGradeToUpdate.getPointsPossible()));
            mCourseSpinner.setSelection(courseAdapter.getPosition(course));

            mComponentAdapter = new ArrayAdapter<GradeComponent>(mContext,
                    android.R.layout.simple_spinner_item, mDatabase.getGradeComponents(course.getId()));
            mComponentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mComponentSpinner.setAdapter(mComponentAdapter);

            mComponentSpinner.setSelection(mComponentAdapter.getPosition(gradeComponent));
        }

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

                    if (mGradeToUpdate == null) {
                        // Insert grade if all the other tests pass.
                        mDatabase.insertGrade(courseId, componentId, gradeName,
                                Double.parseDouble(gradeReceived), Double.parseDouble(gradePossible));
                        // Notify listeners that grade is saved.
                        if (mListener != null) mListener.onGradeSaved(true);
                    } else {
                        // Update grade.
                        mDatabase.updateGrade(mGradeToUpdate.getId(), courseId, componentId,
                                gradeName,
                                Double.parseDouble(gradeReceived),
                                Double.parseDouble(gradePossible));
                        // Notify listeners that grade is updated.
                        if (mListener != null) mListener.onGradeSaved(false);
                    }

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
            mListener = (OnDialogGradeListener) activity;
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
    public static interface OnDialogGradeListener {

        /**
         * Called when a grade is saved.
         */
        public void onGradeSaved(boolean isNew);

    }
}
