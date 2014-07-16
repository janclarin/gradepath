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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.GradeComponent;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.List;

public class EditCourseActivity extends BaseActivity {

    // Tags used to check state of a button.
    public static final String ADD_TAG = "Add";
    public static final String REMOVE_TAG = "Remove";
    public static final String LOG_TAG = EditCourseActivity.class.getSimpleName();

    private Spinner mSemesterSpinner;
    private EditText mNameEditText;
    private RadioGroup mCompletedRadioGroup;
    private TextView mGradeTextView;
    private SeekBar mGradeSeeker;
    private LinearLayout mComponentList;

    private Course mCourseToUpdate;

    // List of grade components.
    private List<GradeComponent> mNewGradeComponents;
    private List<GradeComponent> mOldGradeComponents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up content view.
        setContentView(R.layout.activity_edit_course);

        // Set up the views.
        setUp();

        // Arguments from activity.
        mCourseToUpdate = (Course) getIntent().getSerializableExtra(MainActivity.COURSE_KEY);

        // Set up grade category list view.
        mNewGradeComponents = new ArrayList<GradeComponent>();

        // Check if there is a Course in arguments. If so, insert course information into views.
        // Update Course.
        if (mCourseToUpdate != null) {
            mOldGradeComponents = mDatabase.getGradeComponents(mCourseToUpdate.getId());
            Log.i(LOG_TAG, "Updating: " + mCourseToUpdate.getName());

            // Set edit text fields to course data.
            mNameEditText.setText(mCourseToUpdate.getName());

            // Check if there was a final grade for this course.
            if (mCourseToUpdate.isCompleted()) {
                // Check the yes radio button.
                mCompletedRadioGroup.check(R.id.rb_new_course_complete_yes);

                // Set seek bar to saved grade.
                try {
                    mGradeSeeker.setProgress(mCourseToUpdate.getFinalGradeValue());
                } catch (NullPointerException e) {
                    // If view is set to gone, this isn't possible.
                }
            }

            // Get semester.
            Semester courseSemester = mDatabase.getSemester(mCourseToUpdate.getSemesterId());

            // Set spinners to proper season and year.
            setSpinnerToValue(mSemesterSpinner, courseSemester);

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
            // Set final grade to no by default.
            mCompletedRadioGroup.check(R.id.rb_new_course_complete_no);

            // Add first grade component by default if none exist.
            addGradeComponent(new GradeComponent(), false);
        }

        // Set up custom action bar.
        setUpCustomActionBar();
    }

    /**
     * Set up views.
     */
    private void setUp() {
        // Find views.
        mSemesterSpinner = (Spinner) findViewById(R.id.spn_semester);
        mNameEditText = (EditText) findViewById(R.id.et_course_name);
        mCompletedRadioGroup = (RadioGroup) findViewById(R.id.rg_completed);
        mGradeTextView = (TextView) findViewById(R.id.tv_letter_grade);
        mGradeSeeker = (SeekBar) findViewById(R.id.seek_letter_grade);
        mComponentList = (LinearLayout) findViewById(R.id.ll_grade_components);

        // Set up semester spinner.
        List<Semester> semesters = mDatabase.getSemesters();
        ArrayAdapter<Semester> adapter = new ArrayAdapter<Semester>(this,
                android.R.layout.simple_spinner_item, semesters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSemesterSpinner.setAdapter(adapter);

        // Close keyboard on spinner press with custom listeners.
        mSemesterSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard.
                InputMethodManager inputMethodManager = (InputMethodManager) EditCourseActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    inputMethodManager.hideSoftInputFromWindow(
                            EditCourseActivity.this.getCurrentFocus().getWindowToken(), 0);
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                return false;
            }
        });

        // Set radio group listener.
        mCompletedRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_new_course_complete_no:
                        // Set visibility of final grade seek bar to gone.
                        mGradeTextView.setVisibility(View.GONE);
                        mGradeSeeker.setVisibility(View.GONE);
                        break;
                    case R.id.rb_new_course_complete_yes:
                        // Set visibility of final grade seek bar to visible.
                        mGradeTextView.setVisibility(View.VISIBLE);
                        mGradeSeeker.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        // Set radio group to default no.
        mCompletedRadioGroup.check(R.id.rb_new_course_complete_no);

        // Set up grade seeker.
        // Set max value to the number of possible letter grades values.
        mGradeSeeker.setMax(Course.LetterGrade.values().length - 1);

        // Increment seek bar by 1.
        mGradeSeeker.setKeyProgressIncrement(1);

        // Show middle seek bar value and text view.
        mGradeTextView.setText(Course.LetterGrade.values()[6].toString());
        mGradeSeeker.setProgress(6);

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
        // TODO: Optimize the checks.
        // Open mDatabase.
        String courseName = mNameEditText.getText().toString();

        // Check if there is a course name. Prompt for one if there isn't one.
        if (courseName.length() > 0) {

            // Course id.
            long courseId;

            // Insert semester into mDatabase to get id. Checks if semester already exists.
            Semester semester = (Semester) mSemesterSpinner.getSelectedItem();

            // Check if there is a course to update. If not, create a new course.
            if (mCourseToUpdate != null) {
                // Set values to update values.
                mCourseToUpdate.setSemesterId(semester.getId());
                mCourseToUpdate.setName(courseName);

                // Attempt, but will fail if the view is gone.
                try {
                    mCourseToUpdate.setFinalGradeValue(mGradeSeeker.getProgress());
                } catch (Exception e) {
                    // If mGradeSeeker is View.GONE, there is no grade yet, so set to 0.
                    mCourseToUpdate.setFinalGradeValue(0);
                }

                // Determine whether the course is completed.
                switch (mCompletedRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_new_course_complete_no:
                        mCourseToUpdate.setCompleted(false);
                        break;
                    case R.id.rb_new_course_complete_yes:
                        mCourseToUpdate.setCompleted(true);
                        break;
                }

                // Update course.
                mDatabase.updateCourse(mCourseToUpdate);

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

                Log.i(LOG_TAG, "Update course complete");

                // Course is a new one not found in mDatabase.
            } else {
                // Letter grade value.
                int gradeValue = mGradeSeeker.getProgress();

                // Determine whether the course is completed.
                boolean isCompleted = mCompletedRadioGroup.getCheckedRadioButtonId() ==
                        R.id.rb_new_course_complete_yes;

                // Course object for new course.
                courseId = mDatabase.insertCourse(semester.getId(), courseName, gradeValue, isCompleted);

                Log.i(LOG_TAG, "New course complete");
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
        } else {
            // Show toast to indicate a course name is missing.
            Toast toast = Toast.makeText(this, getString(R.string.prompt_enter_course_name),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    /**
     * Adds a new grade component, including all of its views.
     *
     * @param gradeComponent
     * @param updating       boolean to indicate if updating grade component.
     */
    private void addGradeComponent(GradeComponent gradeComponent, boolean updating) {

        // View holder for grade component.
        GradeComponentViewHolder viewHolder = new GradeComponentViewHolder();

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

        // Sets listeners for edit text fields and button.
        setComponentTextChangedListeners(viewHolder);
        setComponentButtonOnClickListener(viewHolder);
    }

    /**
     * Sets text change listeners for edit text fields in grade component layout.
     *
     * @param viewHolder
     */
    private void setComponentTextChangedListeners(final GradeComponentViewHolder viewHolder) {

        // Add text change listener for component name..
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
    }

    /**
     * Sets button on click listeners for add/remove buttons for grade components.
     *
     * @param viewHolder
     */
    private void setComponentButtonOnClickListener(final GradeComponentViewHolder viewHolder) {

        // Sets button on click listener.
        viewHolder.btnAddRemove.setOnClickListener(
                new ComponentOnClickListener(viewHolder) {
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
    private void setSpinnerToValue(Spinner spinner, Semester semester) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        spinner.setSelection(adapter.getPosition(semester));
    }

    /**
     * On click listener for each component.
     */
    public class ComponentOnClickListener implements View.OnClickListener {

        private EditCourseActivity.GradeComponentViewHolder viewHolder;

        public ComponentOnClickListener(EditCourseActivity.GradeComponentViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            // Overridden in list mAdapter.
        }
    }

    /**
     * View holder class for grade components.
     */
    class GradeComponentViewHolder {
        GradeComponent gradeComponent;
        RelativeLayout layout;
        EditText etName;
        EditText etWeight;
        EditText etNumberOfItems;
        ImageButton btnAddRemove;
    }
}
