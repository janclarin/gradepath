package com.janclarin.gradepath.activity;

import android.app.Activity;
import android.os.Bundle;

import com.janclarin.gradepath.database.DatabaseFacade;

abstract public class BaseActivity extends Activity {

    protected static final String NEW_SEMESTER_TAG = "NewSemesterDialog";
    protected static final String EDIT_SEMESTER_TAG = "EditSemesterDialog";
    protected static final String NEW_GRADE_TAG = "NewGradeDialog";
    protected static final String EDIT_GRADE_TAG = "EditGradeDialog";
    protected static final String NEW_TASK_TAG = "NewTaskDialog";
    protected static final String EDIT_TASK_TAG = "EditTaskDialog";
    public static final String SEMESTER_KEY = "Semester";
    public static final String COURSE_KEY = "Course";
    public static final String GRADE_KEY = "Grade";
    public static final String TASK_KEY = "Task";

    protected DatabaseFacade mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize database.
        mDatabase = DatabaseFacade.getInstance(getApplicationContext());
        mDatabase.open();
    }

}
