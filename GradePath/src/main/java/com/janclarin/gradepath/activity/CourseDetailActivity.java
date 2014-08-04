package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;

public class CourseDetailActivity extends BaseActivity {

    private static final String LOG_TAG = CourseDetailActivity.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private ViewPager mViewPager;
    private Course mCourse;
    private Grade mGrade;
    private Reminder mReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mCourse = (Course) getIntent().getSerializableExtra(COURSE_KEY);
        mGrade = (Grade) getIntent().getSerializableExtra(GRADE_KEY);
        mReminder = (Reminder) getIntent().getSerializableExtra(REMINDER_KEY);

        final ActionBar actionBar = getActionBar();

        // Set action bar title to course name.
        actionBar.setTitle(mCourse.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_LIST_COURSE_EDIT_COURSE) {
            // Update course item and update action bar title.
            mCourse = mDatabase.getCourse(mCourse.getId());
            getActionBar().setTitle(mCourse.getName());

            Toast.makeText(this, R.string.course_saved_update, Toast.LENGTH_SHORT).show();
        }
    }
}
