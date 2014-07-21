package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.GradeComponent;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.List;

public class CourseEditActivity extends BaseActivity
        implements SemesterDialogFragment.OnDialogSemesterCallbacks {

    // Tags used to check state of a button.
    public static final String ADD_TAG = "Add";
    public static final String REMOVE_TAG = "Remove";
    public static final String LOG_TAG = CourseEditActivity.class.getSimpleName();

    private final Semester mNewSemesterIndicator = new Semester() {
        @Override
        public String toString() {
            return getString(R.string.new_semester);
        }
    };

    private EditText mCourseName;
    private EditText mInstructorName;
    private EditText mInstructorEmail;
    private Spinner mSemesterSpinner;
    private CheckBox mCompletedCheckBox;
    private TextView mSectionTwoHeader;
    private TextView mGradeTextView;
    private SeekBar mGradeSeeker;
    private LinearLayout mComponentList;
    private TextView mComponentListHeader;

    private Course mCourseToUpdate;
    private ArrayAdapter<Semester> mAdapter;

    // List of grade components.
    private List<GradeComponent> mNewGradeComponents;
    private List<GradeComponent> mOldGradeComponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up content view.
        setContentView(R.layout.activity_edit_course);

        // Set up the views.
        setUpView();

        // Arguments from activity.
        mCourseToUpdate = (Course) getIntent().getSerializableExtra(MainActivity.COURSE_KEY);

        // Set up grade category list view.
        mNewGradeComponents = new ArrayList<GradeComponent>();

        // Check if there is a Course in arguments. If so, insert course information into views.
        // Update Course.
        if (mCourseToUpdate != null) {
            mOldGradeComponents = mDatabase.getGradeComponents(mCourseToUpdate.getId());

            // Set edit text fields to course data.
            mCourseName.setText(mCourseToUpdate.getName());
            mInstructorName.setText(mCourseToUpdate.getInstructorName());
            mInstructorEmail.setText(mCourseToUpdate.getInstructorEmail());

            boolean isCompleted = mCourseToUpdate.isCompleted();
            mCompletedCheckBox.setChecked(isCompleted);
            // Check if there was a final grade for this course.
            if (isCompleted) {
                // Set seek bar to saved grade.
                mGradeSeeker.setProgress(mCourseToUpdate.getFinalGradeValue());
            }

            // Get semester.
            Semester courseSemester = mDatabase.getSemester(mCourseToUpdate.getSemesterId());

            // Set spinners to proper season and year.
            ArrayAdapter adapter = (ArrayAdapter) mSemesterSpinner.getAdapter();
            mSemesterSpinner.setSelection(adapter.getPosition(courseSemester));

            // Checks if there are any grade components.
            if (mOldGradeComponents.size() == 0) {
                // False because it is going to be a new grade component.
                addGradeComponent(new GradeComponent(), false);
            } else {
                // Add grade component view for each existing grade component.
                for (GradeComponent gradeComponent : mOldGradeComponents)
                    addGradeComponent(gradeComponent, true);
            }
        } else {
            // Add first grade component by default if none exist.
            addGradeComponent(new GradeComponent(), false);
        }

        // Set up custom action bar.
        setUpCustomActionBar();
    }

    @Override
    public void onSemesterSaved(boolean isNew, Semester semester) {
        // Display toast to notify user that the semester is saved.
        Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_semester_saved, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // Remove new semester indicator. Add new semester, then re-add new semester indicator.
        mAdapter.remove(mNewSemesterIndicator);
        mAdapter.add(semester);
        mAdapter.add(mNewSemesterIndicator);
        mAdapter.notifyDataSetChanged();
        mSemesterSpinner.setSelection(mAdapter.getPosition(semester));
    }

    private void newSemester() {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_new_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    /**
     * Set up views.
     */
    private void setUpView() {
        // Find views.
        mCourseName = (EditText) findViewById(R.id.et_course_name);
        mInstructorName = (EditText) findViewById(R.id.et_instructor_name);
        mInstructorEmail = (EditText) findViewById(R.id.et_instructor_email);
        mSemesterSpinner = (Spinner) findViewById(R.id.spn_semester);
        mCompletedCheckBox = (CheckBox) findViewById(R.id.cb_course_completed);
        mSectionTwoHeader = (TextView) findViewById(R.id.tv_section_two_header);
        mGradeTextView = (TextView) findViewById(R.id.tv_letter_grade);
        mGradeSeeker = (SeekBar) findViewById(R.id.seek_letter_grade);
        mComponentList = (LinearLayout) findViewById(R.id.ll_grade_components);

        if (!mCompletedCheckBox.isChecked()) {
            // Set visibility of final grade seek bar to gone.
            mSectionTwoHeader.setText(R.string.grade_components);
            mGradeTextView.setVisibility(View.GONE);
            mGradeSeeker.setVisibility(View.GONE);
        }

        // Set up semester spinner.
        List<Semester> semesters = mDatabase.getSemesters();

        // New semester object as an indication to open new semester dialog.
        semesters.add(mNewSemesterIndicator);

        mAdapter = new ArrayAdapter<Semester>(this,
                android.R.layout.simple_spinner_item, semesters);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSemesterSpinner.setAdapter(mAdapter);

        // Set selection to current semester if available.
        Semester currentSemester = mDatabase.getCurrentSemester();
        if (currentSemester != null) {
            mSemesterSpinner.setSelection(mAdapter.getPosition(currentSemester));
        }

        mSemesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int lastIndex = parent.getAdapter().getCount() - 1;
                if (position == lastIndex) {
                    newSemester();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Close keyboard on spinner press with custom listeners.
        mSemesterSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard.
                InputMethodManager inputMethodManager = (InputMethodManager) CourseEditActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inputMethodManager.hideSoftInputFromWindow(
                            CourseEditActivity.this.getCurrentFocus().getWindowToken(), 0);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // Set radio group listener.
        mCompletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Set visibility of final grade seek bar to visible.
                    mSectionTwoHeader.setText(R.string.final_grade);
                    mSectionTwoHeader.setVisibility(View.VISIBLE);
                    mGradeTextView.setVisibility(View.VISIBLE);
                    mGradeSeeker.setVisibility(View.VISIBLE);
                    mComponentList.setVisibility(View.INVISIBLE);
                } else {
                    // Set visibility of final grade seek bar to gone. Set text for header.
                    mSectionTwoHeader.setText(R.string.grade_components);
                    mGradeTextView.setVisibility(View.GONE);
                    mGradeSeeker.setVisibility(View.GONE);
                    mComponentList.setVisibility(View.VISIBLE);
                }
            }
        });

        // Set up grade seeker.
        // Set max value to the number of possible letter grades values.
        mGradeSeeker.setMax(Course.LetterGrade.values().length - 1);
        mGradeSeeker.setKeyProgressIncrement(1);
        mGradeSeeker.setProgress(6);
        mGradeTextView.setText(Course.LetterGrade.values()[6].toString());

        // Set seek bar change listener.
        mGradeSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Set text view to display corresponding
                mGradeTextView.setText(Course.LetterGrade.values()[progress].toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Saves course with information from new course activity.
     */
    private void saveCourse() {
        String courseName = mCourseName.getText().toString().trim();

        // Check if there is a course name. Prompt for one if there isn't one.
        if (courseName.isEmpty()) {
            Toast toast = Toast.makeText(this, getString(R.string.prompt_enter_course_name),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String instructorName = mInstructorName.getText().toString().trim();
        String instructorEmail = mInstructorEmail.getText().toString().trim();

        // Insert semester into Database to get id. Checks if semester already exists.
        Semester semester = (Semester) mSemesterSpinner.getSelectedItem();

        // Check if this is a proper semester.
        if (semester.toString().equals(getString(R.string.new_semester))) {
            Toast.makeText(this, R.string.pick_semester, Toast.LENGTH_SHORT).show();
            return;
        }

        // Set grade value to -1 if the course is not completed.
        int gradeValue = mCompletedCheckBox.isChecked() ? mGradeSeeker.getProgress() : -1;

        long courseId;
        if (mCourseToUpdate == null) {
            // Course is a new one not found in Database.
            courseId = mDatabase.insertCourse(semester.getId(), courseName, instructorName,
                    instructorEmail, gradeValue, mCompletedCheckBox.isChecked());
        } else {
            // Update course.
            mDatabase.updateCourse(mCourseToUpdate.getId(), semester.getId(), courseName,
                    instructorName, instructorEmail, gradeValue, mCompletedCheckBox.isChecked());

            // Update all grade components.
            for (GradeComponent gradeComponent : mOldGradeComponents) {
                // If update fails, component doesn't exist yet. Insert it instead.
                String name = gradeComponent.getName();
                double weight = gradeComponent.getWeight();
                int numOfItems = gradeComponent.getNumberOfItems();

                // Ensure that the grade component fields are valid.
                if (name != null && weight > 0 && numOfItems > 0) {
                    mDatabase.updateGradeComponent(gradeComponent);
                }
            }
            // Set course id.
            courseId = mCourseToUpdate.getId();
        }

        // Add new grade components.
        for (GradeComponent gradeComponent : mNewGradeComponents) {
            String name = gradeComponent.getName();
            double weight = gradeComponent.getWeight();
            int numOfItems = gradeComponent.getNumberOfItems();

            // Ensure that the grade component fields are valid.
            if (name != null && weight > 0 && numOfItems > 0) {
                mDatabase.insertGradeComponent(courseId, name, weight, numOfItems);
            }
        }

        setResult(RESULT_OK);
        finish();
    }

    /**
     * Adds a new grade component, including all of its views.
     *
     * @param gradeComponent
     * @param updating       boolean to indicate if updating grade component.
     */
    private void addGradeComponent(GradeComponent gradeComponent, boolean updating) {

        // View holder for grade component.
        final ViewHolder viewHolder = new ViewHolder();

        // Inflate first grade component.
        viewHolder.layout = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.activity_edit_course_component, mComponentList, false);

        // Initialize grade component and views.
        viewHolder.gradeComponent = gradeComponent;
        viewHolder.etName = (EditText) viewHolder.layout.findViewById(R.id.et_grade_component_name);
        viewHolder.etWeight = (EditText) viewHolder.layout.findViewById(R.id.et_grade_component_weight);
        viewHolder.etNumberOfItems = (EditText) viewHolder.layout.findViewById(R.id.et_grade_component_number_of_items);
        viewHolder.btnAddRemove = (ImageButton) viewHolder.layout.findViewById(R.id.btn_grade_component);

        // Add view to dynamic linear layout with corresponding identifier.
        mComponentList.addView(viewHolder.layout);

        // Check if updating course.
        if (updating) {
            // Set button tag to add if it is the last grade component.
            if (mOldGradeComponents.indexOf(gradeComponent) == mOldGradeComponents.size() - 1) {
                viewHolder.btnAddRemove.setTag(ADD_TAG);
            } else {
                // Set button to remove.
                viewHolder.btnAddRemove.setTag(REMOVE_TAG);
                viewHolder.btnAddRemove.setImageResource(R.drawable.ic_grade_component_remove);
            }

            // Set grade component views.
            viewHolder.etName.setText(gradeComponent.getName());
            viewHolder.etWeight.setText(Double.toString(gradeComponent.getWeight()));
            viewHolder.etNumberOfItems.setText(Integer.toString(gradeComponent.getNumberOfItems()));
        } else {
            viewHolder.btnAddRemove.setTag(ADD_TAG);

            // Add grade component to list.
            mNewGradeComponents.add(gradeComponent);

            // Request focus to view if there is more than one component.
            if (mNewGradeComponents.size() > 1) viewHolder.layout.requestFocus();
        }

        // Add text change listener for component name.
        viewHolder.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewHolder.gradeComponent.setName(s.toString().trim());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add text change listener for weight.
        viewHolder.etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    viewHolder.gradeComponent.setWeight(Double.parseDouble(s.toString()));
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Error reading double value: " + s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add text change listener for number of items.
        viewHolder.etNumberOfItems.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    viewHolder.gradeComponent.setNumberOfItems(Integer.parseInt(s.toString()));
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Error reading double value: " + s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Sets button on click listener.
        viewHolder.btnAddRemove.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageButton button = viewHolder.btnAddRemove;

                        // Checks if button is set to add.
                        if (button.getTag().equals(ADD_TAG)) {
                            // Adds new grade component.
                            addGradeComponent(new GradeComponent(), false);

                            // Changes button to remove button.
                            button.setImageResource(R.drawable.ic_grade_component_remove);

                            // Changes button tag to remove.
                            button.setTag(REMOVE_TAG);
                        } else {
                            // Remove grade component from linear layout list.
                            mComponentList.removeView(viewHolder.layout);

                            // Remove grade component from mDatabase.
                            if (mCourseToUpdate != null) {
                                mDatabase.deleteGradeComponent(viewHolder.gradeComponent);
                            }

                            // Removes grade component from list of grade components.
                            mNewGradeComponents.remove(viewHolder.gradeComponent);
                        }
                    }
                }
        );
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

        // If updating course, set positive button to "Update", otherwise just "Save"
        View positiveButton = customActionBarView.findViewById(R.id.actionbar_done);
        String positiveButtonText = (mCourseToUpdate != null) ? getString(R.string.actionbar_update) : getString(R.string.actionbar_save);
        ((TextView) positiveButton.findViewById(R.id.tv_actionbar_done)).setText(positiveButtonText);

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

    /**
     * Sets spinner item based on value.
     *
     * @param spinner
     * @param semester
     */

    /**
     * View holder class for grade components.
     */
    private class ViewHolder {
        GradeComponent gradeComponent;
        RelativeLayout layout;
        EditText etName;
        EditText etWeight;
        EditText etNumberOfItems;
        ImageButton btnAddRemove;
    }
}
