package com.janclarin.gradepath.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.FinalGradeDialogFragment;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.fragment.BaseListFragment;
import com.janclarin.gradepath.fragment.ListCourseFragment;
import com.janclarin.gradepath.fragment.ListGradeFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;

public class ListFragmentActivity extends BaseActivity
        implements ListGradeFragment.OnFragmentListGradeListener,
        ListCourseFragment.OnFragmentListCourseListener,
        ListSemesterFragment.OnFragmentListSemesterListener,
        GradeDialogFragment.OnDialogGradeListener,
        SemesterDialogFragment.OnDialogSemesterListener,
        FinalGradeDialogFragment.Callbacks {

    public static final String FRAGMENT_TYPE = "Fragment";
    BaseListFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_fragment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        int title = 0;

        int fragmentType = getIntent().getIntExtra(FRAGMENT_TYPE, 0);

        switch (fragmentType) {
            case 0:
                mCurrentFragment = ListGradeFragment.newInstance();
                title = R.string.title_fragment_list_grades;
                break;
            case 1:
                mCurrentFragment = ListCourseFragment.newInstance();
                title = R.string.title_fragment_list_courses;
                break;
            case 2:
                mCurrentFragment = ListSemesterFragment.newInstance();
                title = R.string.title_fragment_list_semesters;
        }

        getActionBar().setTitle(title);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if request was successful.
        if (resultCode == RESULT_OK) {
            String toastMessage = "";

            switch (requestCode) {
                case REQUEST_LIST_COURSE_NEW_COURSE:
                    refreshList();
                    toastMessage = getString(R.string.course_saved_new);
                    break;
                case REQUEST_LIST_COURSE_EDIT_COURSE:
                    refreshList();
                    toastMessage = getString(R.string.course_saved_update);
                    break;
            }

            // Display toast.
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshList() {
        mCurrentFragment.updateListItems();
    }

    @Override
    public void onListCourseViewDetails(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivity(intent);
    }

    @Override
    public void onListCourseSetFinalGrade(Course course) {
        FinalGradeDialogFragment finalGradeDialog = FinalGradeDialogFragment.newInstance(
                getString(R.string.title_final_grade_dialog), course);
        finalGradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onListGradeEdit(Grade grade) {
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

        refreshList();
    }

    @Override
    public void onListSemesterNew() {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterEdit(Semester semester) {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_semester_dialog), semester);
        semesterDialog.show(getFragmentManager(), EDIT_SEMESTER_TAG);
    }

    @Override
    public void onSemesterSaved(boolean isNew, Semester semester) {
        // String is set to "semester saved" if grade is new, if updating "semester updated."
        String toastMessage = isNew ? getString(R.string.toast_semester_saved) :
                getString(R.string.toast_semester_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        // Refresh the semester list.
        refreshList();
    }

    @Override
    public void onFinalGradeSaved(boolean isNew) {
        String toastMessage = isNew
                ? getString(R.string.toast_final_grade_saved)
                : getString(R.string.toast_final_grade_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }
}
