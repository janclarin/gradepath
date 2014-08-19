package com.janclarin.gradepath.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.fragment.CourseDetailFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;

public class CourseDetailActivity extends BaseActivity
        implements CourseDetailFragment.Callbacks,
        GradeDialogFragment.OnDialogGradeListener {

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

    @Override
    public void onEditGrade(Grade grade) {

        // Show edit grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onGradeSaved(boolean isNew) {
        // String is set to "grade saved" if grade is new, if updating "grade updated."
        String toastMessage = isNew ? getString(R.string.toast_grade_saved) :
                getString(R.string.toast_grade_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        // Update grades list.
        mFragment.updateListItems();
    }
}
