package com.janclarin.gradepath.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.fragment.CourseDetailFragment;
import com.janclarin.gradepath.model.Course;

public class CourseDetailActivity extends BaseActivity
        implements CourseDetailFragment.Callbacks {

    private static final String LOG_TAG = CourseDetailActivity.class.getSimpleName();

    private Course mCourse;

    private CourseDetailFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mCourse = (Course) getIntent().getSerializableExtra(COURSE_KEY);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mFragment = CourseDetailFragment.newInstance(mCourse);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_LIST_COURSE_EDIT_COURSE) {
            // Update course item and send it to fragment.
            mCourse = mDatabase.getCourse(mCourse.getId());
            mFragment.onCourseUpdated(mCourse);

            Toast.makeText(this, R.string.course_saved_update, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditCourse(Course course) {
        Intent intent = new Intent(this, CourseEditActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivityForResult(intent, REQUEST_LIST_COURSE_EDIT_COURSE);
    }
}
