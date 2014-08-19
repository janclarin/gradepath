package com.janclarin.gradepath.activity;

import android.app.Activity;
import android.os.Bundle;

import com.janclarin.gradepath.database.DatabaseFacade;

abstract public class BaseActivity extends Activity {
    protected static final String NEW_SEMESTER_TAG = "NewSemesterDialog";
    protected static final String EDIT_SEMESTER_TAG = "EditSemesterDialog";
    protected static final String NEW_GRADE_TAG = "NewGradeDialog";
    protected static final String EDIT_GRADE_TAG = "EditGradeDialog";
    protected static final String NEW_GRADE_COMPONENT_TAG = "NewGradeComponentDialog";
    protected static final String EDIT_GRADE_COMPONENT_TAG = "EditGradeComponentDialog";
    public static final int REQUEST_LIST_COURSE_NEW_COURSE = 101;
    public static final int REQUEST_LIST_COURSE_EDIT_COURSE = 102;
    public static final String SEMESTER_KEY = "Semester";
    public static final String COURSE_KEY = "Course";
    public static final String GRADE_KEY = "Grade";

    protected DatabaseFacade mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize database.
        mDatabase = DatabaseFacade.getInstance(getApplicationContext());
        mDatabase.open();
    }

}
