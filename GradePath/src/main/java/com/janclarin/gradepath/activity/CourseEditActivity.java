package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeComponentDialogFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.GradeComponent;
import com.janclarin.gradepath.model.Semester;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CourseEditActivity extends BaseActivity
        implements GradeComponentDialogFragment.OnDialogGradeComponentListener {

    public static final String LOG_TAG = CourseEditActivity.class.getSimpleName();

    private EditText mCourseName;
    private EditText mInstructorName;
    private EditText mInstructorEmail;
    private Spinner mCredits;
    private LinearLayout mComponentList;

    private Course mCourseToUpdate;
    private int updatePosition = -1;

    // List of grade components.
    private List<GradeComponent> mGradeComponents;
    private List<GradeComponent> mRemoveComponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up content view.
        setContentView(R.layout.activity_edit_course);

        // Set up custom action bar.
        setUpCustomActionBar();

        // Arguments from activity.
        mCourseToUpdate = (Course) getIntent().getSerializableExtra(COURSE_KEY);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Find views.
        mCourseName = (EditText) findViewById(R.id.et_course_name);
        mInstructorName = (EditText) findViewById(R.id.et_instructor_name);
        mInstructorEmail = (EditText) findViewById(R.id.et_instructor_email);
        mCredits = (Spinner) findViewById(R.id.spn_course_credits);
        mComponentList = (LinearLayout) findViewById(R.id.ll_grade_components);

        // Set add button to open new grade component dialog.
        findViewById(R.id.btn_add_grade_component).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGradeComponent();
            }
        });

        Double[] credits = new Double[]{3.0, 6.0};
        ArrayAdapter<Double> adapter =
                new ArrayAdapter<Double>(this, R.layout.spinner_item, credits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mCredits.setAdapter(adapter);

        // Set up grade category list view.
        mGradeComponents = new ArrayList<GradeComponent>();
        mRemoveComponents = new ArrayList<GradeComponent>();

        // Check if there is a Course in arguments. If so, insert course information into views.
        if (mCourseToUpdate != null) {
            mGradeComponents = mDatabase.getGradeComponents(mCourseToUpdate.getId());

            // Set edit text fields to course data.
            mCourseName.setText(mCourseToUpdate.getName());
            mInstructorName.setText(mCourseToUpdate.getInstructorName());
            mInstructorEmail.setText(mCourseToUpdate.getInstructorEmail());
            mCredits.setSelection(adapter.getPosition(mCourseToUpdate.getCredits()));

            // Add grade component view for each existing grade component.
            for (GradeComponent gradeComponent : mGradeComponents)
                addGradeComponent(gradeComponent);
        }
    }

    private void newGradeComponent() {
        GradeComponentDialogFragment gradeComponentDialog = GradeComponentDialogFragment.newInstance(
                getString(R.string.title_grade_component_dialog));
        gradeComponentDialog.show(getFragmentManager(), NEW_GRADE_COMPONENT_TAG);
    }

    private void editGradeComponent(GradeComponent gradeComponent) {
        updatePosition = mGradeComponents.indexOf(gradeComponent);
        GradeComponentDialogFragment gradeComponentDialog = GradeComponentDialogFragment.newInstance(
                getString(R.string.title_grade_component_dialog), gradeComponent);
        gradeComponentDialog.show(getFragmentManager(), EDIT_GRADE_COMPONENT_TAG);
    }

    /**
     * Set up views.
     */
    private void setUpView() {
    }

    private View createGradeComponentView(final GradeComponent gradeComponent) {
        // Inflate grade component view.
        View gradeComponentView = getLayoutInflater().inflate(R.layout.list_item_course_component,
                mComponentList, false);

        ((TextView) gradeComponentView.findViewById(R.id.tv_title)).setText(gradeComponent.getName());
        ((TextView) gradeComponentView.findViewById(R.id.tv_subtitle))
                .setText(Integer.toString(gradeComponent.getNumberOfItems()) + " "
                        + getString(R.string.items));
        ((TextView) gradeComponentView.findViewById(R.id.tv_detail))
                .setText(new DecimalFormat("#.##").format(gradeComponent.getWeight()) + "%");

        // Set edit button on click listener to edit grade component.
        gradeComponentView.findViewById(R.id.btn_edit_grade_component)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editGradeComponent(gradeComponent);
                    }
                });

        // Set remove button on click listener to remove grade component.
        gradeComponentView.findViewById(R.id.btn_remove_grade_component)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = mGradeComponents.indexOf(gradeComponent);
                        mRemoveComponents.add(gradeComponent);
                        mGradeComponents.remove(position);
                        mComponentList.removeViewAt(position);
                    }
                });

        return gradeComponentView;
    }

    /**
     * Add grade component view to the list.
     */
    private void addGradeComponent(final GradeComponent gradeComponent) {

        View gradeComponentView = createGradeComponentView(gradeComponent);

        // Add view to position of grade component in list if its being updated.
        // Otherwise add it to the end of the list.
        if (gradeComponent.getId() > -1) {
            mComponentList.addView(gradeComponentView, mGradeComponents.indexOf(gradeComponent));
        } else {
            mComponentList.addView(gradeComponentView);
        }
    }

    /**
     * Updates grade component view in the list.
     */
    private void updateGradeComponent(final GradeComponent gradeComponent) {
        View gradeComponentView = createGradeComponentView(gradeComponent);

        // Insert view into old spot and remove the one after it as it is the old one.
        mComponentList.removeViewAt(updatePosition);
        if (updatePosition < mGradeComponents.size()) {
            mGradeComponents.add(updatePosition, gradeComponent);
            mComponentList.addView(gradeComponentView, updatePosition);
        } else {
            mGradeComponents.add(updatePosition, gradeComponent);
            mComponentList.addView(gradeComponentView);
        }
    }

    /**
     * Saves course with information from new course activity.
     */
    private void saveCourse() {
        // Insert semester into Database to get id. Checks if semester already exists.
        Semester semester = (Semester) getIntent().getSerializableExtra(SEMESTER_KEY);
        String courseName = mCourseName.getText().toString().trim();
        String instructorName = mInstructorName.getText().toString().trim();
        String instructorEmail = mInstructorEmail.getText().toString().trim();

        String toastMessage = "";
        if (courseName.isEmpty())
            toastMessage = getString(R.string.prompt_enter_course_name);

        if (toastMessage.length() > 0) {
            Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        long courseId;
        if (mCourseToUpdate == null) {
            // Course is a new one not found in Database.
            courseId = mDatabase.insertCourse(semester.getId(), courseName, instructorName,
                    instructorEmail, (Double) mCredits.getSelectedItem(), -1);
        } else {
            // Update course.
            courseId = mCourseToUpdate.getId();
            mDatabase.updateCourse(courseId, mCourseToUpdate.getSemesterId(), courseName,
                    instructorName, instructorEmail, (Double) mCredits.getSelectedItem(),
                    mCourseToUpdate.getFinalGradeValue());
        }

        for (GradeComponent gradeComponent : mGradeComponents) {
            // Insert grade component into database if there isn't one to update.
            if (gradeComponent.getId() == -1) {
                mDatabase.insertGradeComponent(courseId, gradeComponent.getName(),
                        gradeComponent.getWeight(), gradeComponent.getNumberOfItems());
            } else {
                // Upgrade grade component.
                mDatabase.updateGradeComponent(gradeComponent.getId(), courseId,
                        gradeComponent.getName(), gradeComponent.getWeight(),
                        gradeComponent.getNumberOfItems());
            }
        }

        // Remove all grade components to be removed.
        for (GradeComponent gradeComponent : mRemoveComponents) {
            mDatabase.deleteGradeComponent(gradeComponent);
        }

        setResult(RESULT_OK);
        finish();
    }

    /**
     * Sets up custom done/cancel action bar.
     */

    private void setUpCustomActionBar() {
        // Inflate done/cancel custom actionbar view.
        final LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_done_cancel, null);

        // Set cancel button on click listener.
        customActionBarView.findViewById(R.id.actionbar_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Close activity.
                        finish();
                    }
                });

        View positiveButton = customActionBarView.findViewById(R.id.actionbar_done);

        // Set done button on click listener.
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save course.
                saveCourse();
            }
        });

        // Show cancel/done action bar and hide normal home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
        );
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
    }

    @Override
    public void onGradeComponentSaved(GradeComponent gradeComponent, boolean isNew) {
        if (isNew) {
            mGradeComponents.add(gradeComponent);
            addGradeComponent(gradeComponent);
        } else {
            // Get position of grade component from list and replace the existing one.
            int position = mGradeComponents.indexOf(gradeComponent);
            mGradeComponents.add(position, gradeComponent);
            updateGradeComponent(gradeComponent);
        }
    }
}
