package com.janclarin.gradepath.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.ReminderDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.fragment.BaseListFragment;
import com.janclarin.gradepath.fragment.ListCourseFragment;
import com.janclarin.gradepath.fragment.ListGradeFragment;
import com.janclarin.gradepath.fragment.ListReminderFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.model.Semester;

public class ListFragmentActivity extends BaseActivity
        implements ListReminderFragment.OnFragmentListTaskListener,
        ListGradeFragment.OnFragmentListGradeListener,
        ListCourseFragment.OnFragmentListCourseListener,
        ListSemesterFragment.OnFragmentListSemesterListener,
        GradeDialogFragment.OnDialogGradeListener,
        ReminderDialogFragment.OnDialogReminderListener,
        SemesterDialogFragment.OnDialogSemesterListener {

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
                mCurrentFragment = ListReminderFragment.newInstance();
                title = R.string.title_fragment_list_reminders;
                break;
            case 1:
                mCurrentFragment = ListGradeFragment.newInstance();
                title = R.string.title_fragment_list_grades;
                break;
            case 2:
                mCurrentFragment = ListCourseFragment.newInstance();
                title = R.string.title_fragment_list_courses;
                break;
            case 3:
                mCurrentFragment = ListSemesterFragment.newInstance();
                title = R.string.title_fragment_list_semesters;
        }

        getActionBar().setTitle(title);

        getSupportFragmentManager()
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
    public void onListCourseEdit(Course course) {
        Intent intent = new Intent(this, CourseEditActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivityForResult(intent, REQUEST_LIST_COURSE_EDIT_COURSE);
    }

    @Override
    public void onListCourseDelete(final Course course) {
        final String title = String.format(getString(R.string.title_delete_course_dialog), course.getName());
        final String positiveMessage =
                String.format(getString(R.string.toast_alert_delete_confirmation), course.getName());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.message_delete_course_dialog)
                .setIcon(R.drawable.ic_action_contextual_delete)
                .setPositiveButton(R.string.btn_alert_delete_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete course, display text, and refresh list.
                        mDatabase.deleteCourse(course.getId());
                        Toast.makeText(getApplicationContext(), positiveMessage, Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                })
                .setNegativeButton(R.string.btn_alert_delete_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, just dismiss.
                    }
                })
                .show();
    }

    @Override
    public void onListGradeEdit(Grade grade) {
        // Show edit grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getSupportFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onListReminderEdit(Reminder reminder) {
        // Show edit reminder dialog.
        ReminderDialogFragment reminderDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_edit_reminder_dialog), reminder);
        reminderDialog.show(getSupportFragmentManager(), EDIT_REMINDER_TAG);
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
    public void onReminderSaved(boolean isNew) {
        String toastMessage = isNew ? getString(R.string.toast_reminder_saved) :
                getString(R.string.toast_reminder_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        refreshList();
    }

    @Override
    public void onListSemesterNew() {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_new_semester_dialog));
        semesterDialog.show(getSupportFragmentManager(), NEW_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterEdit(Semester semester) {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_edit_semester_dialog), semester);
        semesterDialog.show(getSupportFragmentManager(), EDIT_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterDelete(final Semester semester) {
        final String title = String.format(getString(R.string.title_delete_semester_dialog), semester.toString());
        final String positiveMessage =
                String.format(getString(R.string.toast_alert_delete_confirmation), semester.toString());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.message_delete_semester_dialog)
                .setIcon(R.drawable.ic_action_contextual_delete)
                .setPositiveButton(R.string.btn_alert_delete_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete course, display text, and refresh list.
                        mDatabase.deleteSemester(semester);
                        Toast.makeText(getApplicationContext(), positiveMessage, Toast.LENGTH_SHORT).show();
                        // Refresh lists.
                        refreshList();
                    }
                })
                .setNegativeButton(R.string.btn_alert_delete_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, just dismiss.
                    }
                })
                .show();
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
}
